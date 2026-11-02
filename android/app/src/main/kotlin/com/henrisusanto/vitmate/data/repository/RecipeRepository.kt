package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.image.ImageDownloader
import com.henrisusanto.vitmate.data.image.ImageUrlResolver
import com.henrisusanto.vitmate.data.local.CalcStateEntity
import com.henrisusanto.vitmate.data.local.FavoriteEntity
import com.henrisusanto.vitmate.data.local.JajanCuanDatabase
import com.henrisusanto.vitmate.data.local.RecipeDetailEntity
import com.henrisusanto.vitmate.data.local.RecipeIndexEntity
import com.henrisusanto.vitmate.data.local.escapeLikePattern
import com.henrisusanto.vitmate.data.remote.RecipeRemoteDataSource
import com.henrisusanto.vitmate.data.remote.RemoteResult
import com.henrisusanto.vitmate.data.remote.dto.PostDto
import android.util.Log
import com.henrisusanto.vitmate.domain.HppMarginMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

/**
 * Repository baca/tulis untuk katalog, detail, dan favorit.
 *
 * Detail selalu memakai urutan: snapshot favorit -> cache detail -> jaringan.
 * Favorit adalah snapshot mandiri, bukan relasi ke recipe_index.
 */
class RecipeRepository(
    private val database: JajanCuanDatabase,
    private val remote: RecipeRemoteDataSource,
    private val imageDownloader: ImageDownloader,
    private val imageUrlResolver: ImageUrlResolver,
    private val coversDirectory: File,
    private val json: Json = RepositoryJson,
    private val clock: () -> Long = System::currentTimeMillis,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecipeCatalog, DetailCatalog {
    private val TAG = "RecipeRepository"
    private val indexDao = database.recipeIndexDao()
    private val detailDao = database.recipeDetailDao()
    private val favoriteDao = database.favoriteDao()
    private val calcStateDao = database.calcStateDao()
    private val toggleMutex = Mutex()

    override fun observeRecipes(
        category: String?,
        query: String,
        limit: Int,
        sessionSeed: Long?
    ): Flow<List<RecipeSummary>> {
        val normalizedCategory = category?.takeIf { it.isNotBlank() }
        val escapedQuery = escapeLikePattern(query.trim())
        val safeLimit = limit.coerceAtLeast(1)
        val rows = if (sessionSeed == null) {
            indexDao.observeList(
                category = normalizedCategory,
                escapedQuery = escapedQuery,
                limit = safeLimit
            )
        } else {
            // F-013: query hanya memakai index `orderIdx, id`; pengacakan dan
            // pemotongan limit dikerjakan di Kotlin agar urutan tetap stabil
            // saat limit bertambah (paginasi).
            indexDao.observeListShuffled(
                category = normalizedCategory,
                escapedQuery = escapedQuery
            ).map { entries -> entries.shuffledFor(sessionSeed).take(safeLimit) }
        }
        // F-026: alokasi HashSet hanya saat daftar id favorit berubah, bukan
        // pada setiap emisi list resep.
        val favoriteIds: Flow<Set<String>> = favoriteDao.observeIds()
            .map { ids -> ids.toHashSet() }
            .distinctUntilChanged()
        return combine(rows, favoriteIds) { recipes, favorites ->
            recipes.map { row -> row.toSummary(isFavorite = row.id in favorites) }
        }
    }

    override fun observeCategories(): Flow<List<String>> = indexDao.observeCategories()

    override fun observeFavoriteRecipes(query: String): Flow<List<RecipeSummary>> =
        favoriteDao.observeFiltered(escapeLikePattern(query.trim()))
            .map { favorites -> favorites.map { it.toSummary() } }
            .distinctUntilChanged()

    override fun observeFavoriteCount(): Flow<Int> = favoriteDao.observeCount()

    override fun observeIsFavorite(recipeId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(recipeId)

    suspend fun isFavorite(recipeId: String): Boolean = favoriteDao.isFavorite(recipeId)

    override suspend fun countRecipes(category: String?, query: String): Int =
        indexDao.countFiltered(
            category = category?.takeIf { it.isNotBlank() },
            escapedQuery = escapeLikePattern(query.trim())
        )

    override suspend fun getDetail(recipeId: String): DetailResult = withContext(ioDispatcher) {
        loadDetail(recipeId)
    }

    /**
     * Menambah favorit dengan snapshot lengkap, atau menghapusnya bila sudah
     * ada. Bila detail belum ada di cache dan jaringan gagal, hasilnya
     * [FavoriteToggleResult.NeedsInternet].
     */
    override suspend fun toggleFavorite(recipeId: String): FavoriteToggleResult =
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
                            slug = post.slug.ifBlank { index?.slug.orEmpty() }
                                .ifBlank { recipeId },
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

    /**
     * Dipanggil SyncManager setelah index diganti. Bila server mengembalikan
     * null/404 atau jaringan gagal, snapshot lama dipertahankan.
     */
    suspend fun refreshFavoriteFromNetwork(recipeId: String) = withContext(ioDispatcher) {
        val favorite = favoriteDao.get(recipeId) ?: return@withContext
        val result = remote.fetchPost(recipeId)
        val payload = (result as? RemoteResult.Success)?.value ?: return@withContext
        val post = payload.dto
        val index = indexDao.get(recipeId)

        val coverUrl = imageUrlResolver.resolveCover(index?.cover)
            ?: imageUrlResolver.coverFor(post)
        val thumbUrl = imageUrlResolver.resolveThumb(index?.thumb, coverUrl)

        var coverLocalPath = favorite.coverLocalPath
        var thumbLocalPath = favorite.thumbLocalPath
        if (coverUrl != null) {
            val directory = favoriteDirectory(recipeId)
            val coverFile = File(directory, COVER_FILE_NAME)
            if (imageDownloader.download(coverUrl, coverFile)) {
                coverLocalPath = coverFile.absolutePath
                val thumbFile = File(directory, THUMB_FILE_NAME)
                thumbLocalPath = if (
                    thumbUrl != null &&
                    thumbUrl != coverUrl &&
                    imageDownloader.download(thumbUrl, thumbFile)
                ) {
                    thumbFile.absolutePath
                } else {
                    coverLocalPath
                }
            }
        }

        favoriteDao.upsert(
            favorite.copy(
                slug = post.slug.ifBlank { index?.slug.orEmpty() }
                    .ifBlank { favorite.slug },
                title = post.title.ifBlank { index?.title.orEmpty() },
                category = post.category.ifBlank { index?.category.orEmpty() },
                tagsText = post.tags.joinToString(SPACE)
                    .ifBlank { index?.tagsText.orEmpty() },
                ingredientsText = post.ingredients
                    .joinToString(SPACE) { it.name.lowercase(Locale.ROOT) }
                    .ifBlank { index?.ingredientsText.orEmpty() },
                estCapital = post.businessAnalysis.estimatedInitialCapital
                    .takeIf { it > 0 } ?: index?.estCapital ?: favorite.estCapital,
                portions = post.defaultPortions.takeIf { it > 0 }
                    ?: index?.portions ?: favorite.portions,
                thumbLocalPath = thumbLocalPath,
                coverLocalPath = coverLocalPath,
                detailJson = payload.rawJson
            )
        )
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
                        source = DetailSource.FAVORITE,
                        // Favorit wajib memakai file lokal agar tetap tampil offline.
                        coverUrl = favorite.coverLocalPath
                    )
                )
            }
        }

        val index = indexDao.get(recipeId)
        val cached = detailDao.get(recipeId)
        if (cached != null) {
            val parsed = parsePost(cached.json)
            if (parsed != null) {
                return DetailResult.Success(
                    RecipeDetailData(
                        post = parsed,
                        rawJson = cached.json,
                        source = DetailSource.CACHE,
                        coverUrl = detailCover(index?.cover, parsed)
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
                        source = DetailSource.NETWORK,
                        coverUrl = detailCover(index?.cover, result.value.dto)
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

    private fun detailCover(indexCover: String?, post: PostDto): String? =
        imageUrlResolver.resolveCover(indexCover) ?: imageUrlResolver.coverFor(post)

    override suspend fun loadCalcState(recipeId: String): SavedCalcState? =
        withContext(ioDispatcher) {
            val entity = calcStateDao.get(recipeId) ?: return@withContext null
            val prices = runCatching {
                json.decodeFromString(ListSerializer(Long.serializer()), entity.pricesJson)
            }.getOrDefault(emptyList())
            SavedCalcState(
                prices = prices,
                portions = entity.portions,
                marginMode = runCatching {
                    HppMarginMode.valueOf(entity.marginMode)
                }.getOrDefault(HppMarginMode.PERCENT),
                marginValue = entity.marginValue
            )
        }

    override suspend fun saveCalcState(recipeId: String, state: SavedCalcState) =
        withContext(ioDispatcher) {
            calcStateDao.upsert(
                CalcStateEntity(
                    recipeId = recipeId,
                    pricesJson = json.encodeToString(
                        ListSerializer(Long.serializer()),
                        state.prices
                    ),
                    portions = state.portions,
                    marginMode = state.marginMode.name,
                    marginValue = state.marginValue
                )
            )
        }


    /**
     * Housekeeping: hapus entri [calc_state] yatim (recipeId tidak ada di
     * [recipe_index] maupun [favorites]). Dipanggil sesekali, misalnya dari
     * SyncManager sesudah index/baru disync.
     */
    suspend fun housekeeping() = withContext(ioDispatcher) {
        calcStateDao.deleteOrphaned()
    }

    private fun parsePost(rawJson: String): PostDto? = runCatching {
        json.decodeFromString(PostDto.serializer(), rawJson)
    }.getOrNull()

    private suspend fun deleteFavoriteFiles(recipeId: String) {
        val directory = favoriteDirectory(recipeId)
        val deleted = directory.deleteRecursively()
        if (!deleted) {
            Log.w(
                TAG,
                "Gagal menghapus cache gambar yatim resep $recipeId (direktori: $directory); file thumbnail/cover mungkin masih ada",
                Exception("deleteRecursively() returned false")
            )
        }
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
            slug = slug.ifBlank { id },
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

    private fun String.toTags(): List<String> =
        split(SPACE).map { it.trim() }.filter { it.isNotEmpty() }

    /**
     * F-013: permutasi deterministik per [seed] tanpa `ORDER BY` aritmetika di
     * SQL. Kunci dihitung modulo prima yang sama sehingga hasilnya stabil dan
     * pemotongan `take(limit)` tetap kompatibel dengan paginasi.
     */
    private fun List<RecipeIndexEntity>.shuffledFor(seed: Long): List<RecipeIndexEntity> =
        sortedWith(
            compareBy<RecipeIndexEntity>(
                { shuffleKey(it.orderIdx, seed) },
                { it.id }
            )
        )

    private fun shuffleKey(orderIdx: Int, seed: Long): Long = Math.floorMod(
        (orderIdx.toLong() % SHUFFLE_PRIME) * (seed % SHUFFLE_PRIME),
        SHUFFLE_PRIME
    )

    companion object {
        const val DETAIL_CACHE_LIMIT = 30
        private const val COVER_FILE_NAME = "cover"
        private const val THUMB_FILE_NAME = "thumb"
        private const val SPACE = " "
        private const val VIRAL_TAG = "viral"
        private const val SHUFFLE_PRIME = 1_000_003L
        private val RepositoryJson: Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }
    }
}
