package com.henrisusanto.vitmate.data.repository

import android.app.Application
import androidx.room.Room
import com.henrisusanto.vitmate.data.image.ImageUrlResolver
import com.henrisusanto.vitmate.data.local.FavoriteEntity
import com.henrisusanto.vitmate.data.local.JajanCuanDatabase
import com.henrisusanto.vitmate.data.local.RecipeDetailEntity
import com.henrisusanto.vitmate.data.local.RecipeIndexEntity
import com.henrisusanto.vitmate.data.remote.FailureKind
import com.henrisusanto.vitmate.data.remote.RemoteResult
import com.henrisusanto.vitmate.data.testutil.FakeImageDownloader
import com.henrisusanto.vitmate.data.testutil.FakeRecipeRemoteDataSource
import com.henrisusanto.vitmate.data.testutil.Fixtures
import com.henrisusanto.vitmate.domain.HppMarginMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RecipeRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var database: JajanCuanDatabase
    private lateinit var remote: FakeRecipeRemoteDataSource
    private lateinit var downloader: FakeImageDownloader
    private lateinit var repository: RecipeRepository
    private lateinit var coversDirectory: File

    private val churrosId = "1065-churros-frozen-ide-jualan"
    private val martabakId = "67-martabak-tahu-ide-jualan-1000an"
    private val esBongkoId = "271-es-bongko-khas-pontianak-bisa-untuk-takjil-ide-jualan"

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            JajanCuanDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remote = FakeRecipeRemoteDataSource()
        downloader = FakeImageDownloader()
        coversDirectory = temporaryFolder.newFolder("covers")
        repository = RecipeRepository(
            database = database,
            remote = remote,
            imageDownloader = downloader,
            imageUrlResolver = ImageUrlResolver(),
            coversDirectory = coversDirectory,
            clock = { 1_000L }
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `detail memakai snapshot favorit lebih dulu`() = runBlocking {
        database.favoriteDao().upsert(
            favorite(churrosId, Fixtures.postJson("churros"))
        )
        database.recipeDetailDao().upsert(
            RecipeDetailEntity(esBongkoId, Fixtures.postJson("es_bongko"), 2L)
        )
        remote.postResults[churrosId] = RemoteResult.Success(Fixtures.post("martabak"))

        val result = repository.getDetail(churrosId)

        assertTrue(result is DetailResult.Success)
        val detail = (result as DetailResult.Success).detail
        assertEquals(DetailSource.FAVORITE, detail.source)
        assertEquals("Churros Frozen Crispy", detail.post.title)
        assertEquals(0, remote.postCalls)
    }

    @Test
    fun `detail memakai cache sebelum jaringan`() = runBlocking {
        database.recipeDetailDao().upsert(
            RecipeDetailEntity(churrosId, Fixtures.postJson("churros"), 2L)
        )
        remote.postResults[churrosId] = RemoteResult.Success(Fixtures.post("martabak"))

        val result = repository.getDetail(churrosId)

        assertEquals(DetailSource.CACHE, (result as DetailResult.Success).detail.source)
        assertEquals("Churros Frozen Crispy", result.detail.post.title)
        assertEquals(0, remote.postCalls)
    }

    @Test
    fun `detail dari jaringan disimpan ke cache`() = runBlocking {
        remote.postResults[churrosId] = RemoteResult.Success(Fixtures.post("churros"))

        val result = repository.getDetail(churrosId)

        assertEquals(DetailSource.NETWORK, (result as DetailResult.Success).detail.source)
        assertEquals(1, remote.postCalls)
        assertEquals(1, database.recipeDetailDao().count())
        assertNotNull(database.recipeDetailDao().get(churrosId))

        val cached = repository.getDetail(churrosId)
        assertEquals(DetailSource.CACHE, (cached as DetailResult.Success).detail.source)
        assertEquals(1, remote.postCalls)
    }

    @Test
    fun `detail tanpa cache dan jaringan gagal menghasilkan error`() = runBlocking {
        remote.postResults[churrosId] = RemoteResult.Failure(
            kind = FailureKind.NETWORK,
            message = "offline"
        )

        val result = repository.getDetail(churrosId)

        assertTrue(result is DetailResult.Error)
        assertTrue((result as DetailResult.Error).message.contains("memuat"))
    }

    @Test
    fun `detail favorit memakai foto lokal agar tampil offline`() = runBlocking {
        database.favoriteDao().upsert(
            favorite(churrosId, Fixtures.postJson("churros"))
        )
        remote.postResults[churrosId] = RemoteResult.Failure(
            kind = FailureKind.NETWORK,
            message = "offline"
        )

        val result = repository.getDetail(churrosId)

        val detail = (result as DetailResult.Success).detail
        assertEquals(DetailSource.FAVORITE, detail.source)
        assertEquals("/tmp/$churrosId-cover", detail.coverUrl)
        assertEquals(0, remote.postCalls)
    }

    @Test
    fun `calc state disimpan dan dimuat ulang`() = runBlocking {
        val saved = SavedCalcState(
            prices = listOf(9_000L, 3_000L, 0L),
            portions = 12,
            marginMode = HppMarginMode.NOMINAL,
            marginValue = 1_500.0
        )

        repository.saveCalcState(churrosId, saved)
        val loaded = repository.loadCalcState(churrosId)

        assertEquals(saved, loaded)
    }

    @Test
    fun `calc state tidak ada menghasilkan null`() = runBlocking {
        assertNull(repository.loadCalcState(churrosId))
    }

    @Test
    fun `toggle favorit menyimpan snapshot dan file lokal`() = runBlocking {
        database.recipeIndexDao().insertAll(listOf(index(churrosId, "Churros Frozen Crispy")))
        remote.postResults[churrosId] = RemoteResult.Success(Fixtures.post("churros"))

        val added = repository.toggleFavorite(churrosId)

        assertEquals(FavoriteToggleResult.Added, added)
        val favorite = checkNotNull(database.favoriteDao().get(churrosId))
        assertEquals("Churros Frozen Crispy", favorite.title)
        assertEquals("kue modal-kecil viral", favorite.tagsText)
        assertEquals(27_500, favorite.estCapital)
        assertEquals(3, favorite.portions)
        assertEquals(Fixtures.postJson("churros"), favorite.detailJson)
        assertTrue(File(favorite.coverLocalPath).exists())
        assertTrue(File(checkNotNull(favorite.thumbLocalPath)).exists())
        assertEquals(2, downloader.downloadedUrls.size)

        val removed = repository.toggleFavorite(churrosId)

        assertEquals(FavoriteToggleResult.Removed, removed)
        assertNull(database.favoriteDao().get(churrosId))
        assertFalse(File(coversDirectory, churrosId).exists())
    }

    @Test
    fun `toggle favorit offline tanpa cache meminta internet`() = runBlocking {
        remote.postResults[churrosId] = RemoteResult.Failure(
            kind = FailureKind.NETWORK,
            message = "offline"
        )

        val result = repository.toggleFavorite(churrosId)

        assertEquals(FavoriteToggleResult.NeedsInternet, result)
        assertNull(database.favoriteDao().get(churrosId))
    }

    @Test
    fun `gagal unduh thumb memakai cover sebagai thumbnail`() = runBlocking {
        database.recipeIndexDao().insertAll(listOf(index(churrosId, "Churros Frozen Crispy")))
        remote.postResults[churrosId] = RemoteResult.Success(Fixtures.post("churros"))
        downloader.failWhen = { url -> url.contains("/thumbs/") }

        repository.toggleFavorite(churrosId)

        val favorite = checkNotNull(database.favoriteDao().get(churrosId))
        assertEquals(favorite.coverLocalPath, favorite.thumbLocalPath)
    }

    @Test
    fun `refresh favorit 404 mempertahankan snapshot lama`() = runBlocking {
        database.favoriteDao().upsert(
            favorite(churrosId, Fixtures.postJson("churros"))
        )
        remote.postResults[churrosId] = RemoteResult.NoData

        repository.refreshFavoriteFromNetwork(churrosId)

        val favorite = checkNotNull(database.favoriteDao().get(churrosId))
        assertEquals("Churros Frozen Crispy", favorite.title)
        assertEquals(Fixtures.postJson("churros"), favorite.detailJson)
        assertTrue(downloader.downloadedUrls.isEmpty())
    }

    @Test
    fun `refresh favorit memperbarui snapshot dari server`() = runBlocking {
        database.favoriteDao().upsert(
            favorite(churrosId, Fixtures.postJson("churros"))
        )
        database.recipeIndexDao().insertAll(
            listOf(index(esBongkoId, "Es Bongko Khas Pontianak"))
        )
        remote.postResults[churrosId] = RemoteResult.Success(Fixtures.post("es_bongko"))

        repository.refreshFavoriteFromNetwork(churrosId)

        val favorite = checkNotNull(database.favoriteDao().get(churrosId))
        assertEquals("Es Bongko Khas Pontianak", favorite.title)
        assertEquals(Fixtures.postJson("es_bongko"), favorite.detailJson)
        assertEquals(41_500, favorite.estCapital)
        assertTrue(File(favorite.coverLocalPath).exists())
    }

    @Test
    fun `snapshot favorit tetap tampil setelah index dihapus`() = runBlocking {
        database.recipeIndexDao().insertAll(listOf(index(churrosId, "Churros Frozen Crispy")))
        remote.postResults[churrosId] = RemoteResult.Success(Fixtures.post("churros"))
        repository.toggleFavorite(churrosId)
        database.recipeIndexDao().clear()

        val favorites = repository.observeFavoriteRecipes().first()
        val detail = repository.getDetail(churrosId)

        assertEquals(1, favorites.size)
        assertEquals("Churros Frozen Crispy", favorites.first().title)
        assertTrue(favorites.first().isFavorite)
        assertEquals(DetailSource.FAVORITE, (detail as DetailResult.Success).detail.source)
        assertEquals(1, remote.postCalls)
    }

    @Test
    fun `status favorit pada daftar mengikuti tabel favorites`() = runBlocking {
        database.recipeIndexDao().insertAll(listOf(index(churrosId, "Churros Frozen Crispy")))
        database.favoriteDao().upsert(favorite(churrosId, Fixtures.postJson("churros")))

        val before = repository.observeRecipes(null, "", 10).first()
        assertTrue(before.single().isFavorite)

        database.favoriteDao().delete(churrosId)
        val after = repository.observeRecipes(null, "", 10).first()

        assertFalse(after.single().isFavorite)
    }

    private fun index(id: String, title: String): RecipeIndexEntity {
        val entry = Fixtures.indexEntry(id)
        return RecipeIndexEntity(
            id = id,
            slug = entry.slug,
            title = title,
            category = entry.category,
            tagsText = entry.tags.joinToString(" "),
            ingredientsText = entry.ingredientsText,
            cover = entry.cover,
            thumb = entry.thumb,
            estCapital = entry.estCapital,
            portions = entry.portions,
            isViral = entry.isViral,
            orderIdx = 0
        )
    }

    private fun favorite(id: String, detailJson: String) = FavoriteEntity(
        id = id,
        favoritedAt = 1L,
        title = "Churros Frozen Crispy",
        category = "kue",
        tagsText = "kue modal-kecil viral",
        ingredientsText = "terigu telur",
        estCapital = 27_500,
        portions = 3,
        thumbLocalPath = "/tmp/$id-thumb",
        coverLocalPath = "/tmp/$id-cover",
        detailJson = detailJson
    )
}
