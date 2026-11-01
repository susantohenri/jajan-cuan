class RecipeRepository(
    private val database: JajanCuanDatabase,
    private val remote: RecipeRemoteDataSource,
    private val imageDownloader: ImageDownloader,
    private val imageUrlResolver: ImageUrlResolver,
    private val coversDirectory: File,
    private val json: Json = RepositoryJson,
    private val clock: () -> Long = System::currentTimeMillis,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecipeCatalog {
    private val indexDao = database.recipeIndexDao()
    private val detailDao = database.recipeDetailDao()
    private val favoriteDao = database.favoriteDao()
    private val toggleMutex = Mutex()

    override
    fun observeRecipes(
        category: String?,
        query: String,
        limit: Int,
        sessionSeed: Long? = null
    ): Flow<List<RecipeSummary>> {
        val normalizedCategory = category?.takeIf { it.isNotBlank() }
        val escapedQuery = escapeLikePattern(query.trim())
        val query = if (sessionSeed == null) {
            indexDao.observeList(
                category = normalizedCategory,
                escapedQuery = escapedQuery,
                limit = limit.coerceAtLeast(1)
            )
        } else {
            indexDao.observeListShuffled(
                category = normalizedCategory,
                escapedQuery = escapedQuery,
                limit = limit.coerceAtLeast(1),
                sessionSeed = sessionSeed
            )
        }
        return combine(
            query,
            favoriteDao.observeIds()
        ) { rows, favoriteIds ->
            val favorites = favoriteIds.toHashSet()
            rows.map { row -> row.toSummary(isFavorite = row.id in favorites) }
        }
    }

    override
    fun observeCategories(): Flow<List<String>> = indexDao.observeCategories()

    override
    fun observeFavoriteRecipes(query: String = ""): Flow<List<RecipeSummary>> =
        favoriteDao.observeFiltered(escapeLikePattern(query.trim()))
            .map { favorites -> favorites.map { it.toSummary() } }

    override
    fun observeFavoriteCount(): Flow<Int> = favoriteDao.observeCount()

    override
    suspend fun countRecipes(category: String?, query: String): Int {
        val escapedQuery = escapeLikePattern(query.trim())
        return indexDao.countFiltered(
            category = category?.takeIf { it.isNotBlank() },
            escapedQuery = escapedQuery
        )
    }

    override
    fun observeIsFavorite(recipeId: String): Flow<Boolean> = favoriteDao.observeIsFavorite(recipeId)

    override
    suspend fun isFavorite(recipeId: String): Boolean = favoriteDao.isFavorite(recipeId)

    suspend fun getDetail(recipeId: String): DetailResult = withContext(ioDispatcher) {
        loadDetail(recipeId)
    }

    /**
 * Menambah favorit dengan snapshot lengkap, atau menghapusnya bila sudah
 * ada. Bila detail belum ada di cache dan jaringan gagal, hasilnya
 * [FavoriteToggleResult.NeedsInternet].
 */
    override
    suspend fun toggleFavorite(recipeId: String): FavoriteToggleResult =
        withContext(ioDispatcher) {
            toggleMutex.withLock {
                try {
                    val existing = favoriteDao.get(recipeId)
                    if (existing != null) {
                        favoriteDao.delete(recipeId)
                        deleteFavoriteFiles(recipeId)
                        return@withLock FavoriteToggleResult.Removed
                    }

                    val detail = loadDetail(recipeId)
                    val data = when (detail) {
                        is DetailResult.Success -> detail.detail
                        DetailResult.NotFound -> return@withLock FavoriteToggleResult.Error(
                            "Resep tidak ditemukan."
                        )
                        is DetailResult.Error -> {
                            return@withLock FavoriteToggleResult.NeedsInternet
                        }
                    }

                    val index = indexDao.get(recipeId)
                    val post = data.post
                    val coverUrl = imageUrlResolver.resolveCover(index?.cover)
                        ?: imageUrlResolver.coverFor(post)
                        ?: return@withLock FavoriteToggleResult.Error(
                            "Foto resep tidak tersedia."
                        )
                    val thumbUrl = imageUrlResolver.resolveThumb(index?.thumb, coverUrl)

                    val directory = favoriteDirectory(recipeId)
                    val coverFile = File(directory, COVER_FILE_NAME)
                    if (!imageDownloader.download(coverUrl, coverFile)) {
                        return@withLock FavoriteToggleResult.Error(
                            "Gagal mengunduh foto resep. Periksa koneksi lalu coba lagi."
                        )
                    }

                    val thumbFile = File(directory, THUMB_FILE_NAME)
                    val thumbPath = if (
                        thumbUrl != null &&
                        thumbUrl != coverUrl &&
                        imageDownloader.download(thumbUrl, thumbFile)
                    ) {
                        thumbFile.absolutePath
                    } else {
                        coverFile.absolutePath
                    }

                    favoriteDao.upsert(
                        FavoriteEntity(
                            id = recipeId,
                            favoritedAt = clock(),
                            title = post.title.ifBlank { index?.title.orEmpty() },
                            category = post.category.ifBlank { index?.category.orEmpty() },
                            tagsText = post.tags.joinToString(SPACE)
                                .ifBlank { index?.tagsText.orEmpty() },
                            ingredientsText = post.ingredients
                                .joinToString(SPACE) { it.name.lowercase(Locale.ROOT) }
                                .ifBlank { index?.ingredientsText.orEmpty() },
                            estCapital = post.businessAnalysis.estimatedInitialCapital
                                .takeIf { it > 0 } ?: index?.estCapital ?: 0,
                            portions = post.defaultPortions.takeIf { it > 0 }
                                ?: index?.portions ?: 1,
                            thumbLocalPath = thumbPath,
                            coverLocalPath = coverFile.absolutePath,
                            detailJson = data.rawJson
                        )
                    )
                    FavoriteToggleResult.Added
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    FavoriteToggleResult.Error(
                        "Gagal menyimpan favorit. Coba lagi."
                    )
                }
            }
        }
    private suspend fun loadDetail(recipeId: String): DetailResult {
        val favorite = favoriteDao.get(recipeId)
        if (favorite != null) {
            val parsed = parsePost(favorite.detailJson)
            if (parsed != null) {
                return DetailResult.Success(
                    RecipeDetailData(
                        post = parsed,
                        rawJson = favorite.detailJson,
                        source = DetailSource.FAVORITE
                    )
                )
            }
        }

        val cached = detailDao.get(recipeId)
        if (cached != null) {
            val parsed = parsePost(cached.json)
            if (parsed != null) {
                return DetailResult.Success(
                    RecipeDetailData(
                        post = parsed,
                        rawJson = cached.json,
                        source = DetailSource.CACHE
                    )
                )
            }
            detailDao.delete(recipeId)
        }

        return when (val result = remote.fetchPost(recipeId)) {
            is RemoteResult.Success -> {
                detailDao.upsert(
                    RecipeDetailEntity(
                        id = recipeId,
                        json = result.value.rawJson,
                        fetchedAt = clock()
                    )
                )
                detailDao.pruneTo(DETAIL_CACHE_LIMIT)
                DetailResult.Success(
                    RecipeDetailData(
                        post = result.value.dto,
                        rawJson = result.value.rawJson,
                        source = DetailSource.NETWORK
                    )
                )
            }
            RemoteResult.NoData -> DetailResult.NotFound
            is RemoteResult.Failure -> DetailResult.Error(
                message = "Tidak dapat memuat resep. Periksa koneksi lalu coba lagi.",
                cause = result.cause
            )
        }
    }

    private fun parsePost(rawJson: String): PostDto? = runCatching {
        json.decodeFromString(PostDto.serializer(), rawJson)
    }.getOrNull()

    private suspend fun deleteFavoriteFiles(recipeId: String) {
        favoriteDirectory(recipeId).deleteRecursively()
    }

    private fun favoriteDirectory(recipeId: String): File =
        File(coversDirectory, safeFileName(recipeId))

    private fun safeFileName(value: String): String =
        value.replace(Regex("[^A-Za-z0-9_-]"), "_")

    private fun RecipeIndexEntity.toSummary(isFavorite: Boolean): RecipeSummary {
        val cover = imageUrlResolver.resolveCover(cover)
        return RecipeSummary(
            id = id,
            slug = slug,
            title = title,
            category = category,
            tags = tagsText.toTags(),
            ingredientsText = ingredientsText,
            coverUrl = cover,
            thumbUrl = imageUrlResolver.resolveThumb(thumb, cover),
            estCapital = estCapital,
            portions = portions,
            isViral = isViral,
            isFavorite = isFavorite
        )
    }

    private fun FavoriteEntity.toSummary(): RecipeSummary {
        val tags = tagsText.toTags()
        return RecipeSummary(
            id = id,
            slug = id,
            title = title,
            category = category,
            tags = tags,
            ingredientsText = ingredientsText,
            coverUrl = coverLocalPath,
            thumbUrl = thumbLocalPath ?: coverLocalPath,
            estCapital = estCapital,
            portions = portions,
            isViral = tags.contains(VIRAL_TAG),
            isFavorite = true
        )
    }
    companion object {
        const val DETAIL_CACHE_LIMIT = 30
        private const val COVER_FILE_NAME = "cover"
        private const val THUMB_FILE_NAME = "thumb"
        private const val SPACE = " "
        private const val VIRAL_TAG = "viral"
        private val RepositoryJson: Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }
    }
}
