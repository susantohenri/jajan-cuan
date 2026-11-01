package com.henrisusanto.vitmate.data.sync

import android.app.Application
import androidx.room.Room
import com.henrisusanto.vitmate.data.local.FavoriteEntity
import com.henrisusanto.vitmate.data.local.JajanCuanDatabase
import com.henrisusanto.vitmate.data.local.RecipeDetailEntity
import com.henrisusanto.vitmate.data.local.RecipeIndexEntity
import com.henrisusanto.vitmate.data.local.UnlockEntity
import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.data.remote.FailureKind
import com.henrisusanto.vitmate.data.remote.RemoteResult
import com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto
import com.henrisusanto.vitmate.data.testutil.FakeRecipeRemoteDataSource
import com.henrisusanto.vitmate.data.testutil.FakeSyncStateStore
import com.henrisusanto.vitmate.data.testutil.Fixtures
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SyncManagerTest {

    private lateinit var database: JajanCuanDatabase
    private lateinit var remote: FakeRecipeRemoteDataSource
    private lateinit var stateStore: FakeSyncStateStore
    private lateinit var manager: SyncManager

    private val refreshedFavorites = mutableListOf<String>()
    private var now = 10_000_000L

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            JajanCuanDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remote = FakeRecipeRemoteDataSource()
        stateStore = FakeSyncStateStore()
        manager = SyncManager(
            database = database,
            remote = remote,
            stateStore = stateStore,
            refreshFavorite = { recipeId -> refreshedFavorites += recipeId },
            metaCheckIntervalHours = { 6L },
            clock = { now }
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `throttle melewati cek meta`() = runBlocking {
        stateStore.lastCheckAt = now

        val result = manager.sync(force = false)

        assertEquals(SyncOutcome.Throttled, result)
        assertEquals(0, remote.metaCalls)
    }

    @Test
    fun `meta sama tidak mengunduh index`() = runBlocking {
        val meta = Fixtures.meta()
        stateStore.version = meta.version
        database.recipeIndexDao().insertAll(listOf(indexRow("lama")))
        remote.metaResult = RemoteResult.Success(meta)

        val result = manager.sync(force = true)

        assertEquals(SyncOutcome.UpToDate, result)
        assertEquals(0, remote.indexCalls)
        assertEquals(1, database.recipeIndexDao().count())
    }

    @Test
    fun `meta berubah mengganti index dan mengosongkan detail`() = runBlocking {
        val meta = Fixtures.meta()
        stateStore.version = "versi-lama"
        database.recipeIndexDao().insertAll(listOf(indexRow("lama")))
        database.recipeDetailDao().upsert(
            RecipeDetailEntity("lama", """{"id":"lama"}""", 1L)
        )
        database.unlockDao().insert(
            UnlockEntity("lama", UnlockFeature.CALCULATOR, 50L)
        )
        database.favoriteDao().upsert(favorite("lama"))
        remote.metaResult = RemoteResult.Success(meta)
        remote.indexResult = RemoteResult.Success(indexMap("churros", "martabak"))

        val result = manager.sync(force = true)

        assertTrue(result is SyncOutcome.Updated)
        assertEquals(2, (result as SyncOutcome.Updated).indexCount)
        assertEquals(2, database.recipeIndexDao().count())
        assertNull(database.recipeDetailDao().get("lama"))
        assertEquals(1, database.unlockDao().count())
        assertNotNull(database.favoriteDao().get("lama"))
        assertEquals(listOf("lama"), refreshedFavorites)
        assertEquals(meta.version, stateStore.version)
        assertEquals(now, stateStore.lastCheckAt)
    }

    @Test
    fun `gagal jaringan memakai cache dan tidak mengubah versi`() = runBlocking {
        stateStore.version = "versi-lama"
        database.recipeIndexDao().insertAll(listOf(indexRow("lama")))
        remote.metaResult = RemoteResult.Failure(
            kind = FailureKind.NETWORK,
            message = "offline"
        )

        val result = manager.sync(force = true)

        assertTrue(result is SyncOutcome.Failed)
        assertEquals(1, database.recipeIndexDao().count())
        assertEquals("versi-lama", stateStore.version)
        assertEquals(0, remote.indexCalls)
    }

    @Test
    fun `gagal unduh index mempertahankan index lama`() = runBlocking {
        stateStore.version = "versi-lama"
        database.recipeIndexDao().insertAll(listOf(indexRow("lama")))
        remote.metaResult = RemoteResult.Success(Fixtures.meta())
        remote.indexResult = RemoteResult.Failure(
            kind = FailureKind.NETWORK,
            message = "offline"
        )

        val result = manager.sync(force = true)

        assertTrue(result is SyncOutcome.Failed)
        assertEquals(1, database.recipeIndexDao().count())
        assertEquals("versi-lama", stateStore.version)
        assertEquals(now, stateStore.lastCheckAt)
    }

    @Test
    fun `index kosong tidak menghapus cache`() = runBlocking {
        stateStore.version = "versi-lama"
        database.recipeIndexDao().insertAll(listOf(indexRow("lama")))
        remote.metaResult = RemoteResult.Success(Fixtures.meta())
        remote.indexResult = RemoteResult.Success(emptyMap())

        val result = manager.sync(force = true)

        assertTrue(result is SyncOutcome.Failed)
        assertEquals(1, database.recipeIndexDao().count())
        assertEquals("versi-lama", stateStore.version)
    }

    @Test
    fun `meta tidak tersedia tidak mengubah waktu cek`() = runBlocking {
        remote.metaResult = RemoteResult.NoData

        val result = manager.sync(force = true)

        assertTrue(result is SyncOutcome.Failed)
        assertEquals(0L, stateStore.lastCheckAt)
    }

    @Test
    fun `unlock tetap ada setelah index diganti detail dibersihkan dan favorit dihapus`() =
        runBlocking {
            val meta = Fixtures.meta()
            stateStore.version = "versi-lama"
            database.unlockDao().insert(
                UnlockEntity("churros", UnlockFeature.PROMO, 77L)
            )
            database.favoriteDao().upsert(favorite("churros"))
            database.recipeDetailDao().upsert(
                RecipeDetailEntity("churros", """{"id":"churros"}""", 1L)
            )
            remote.metaResult = RemoteResult.Success(meta)
            remote.indexResult = RemoteResult.Success(indexMap("churros"))

            manager.sync(force = true)
            database.favoriteDao().delete("churros")
            database.recipeDetailDao().clear()

            assertEquals(1, database.unlockDao().count())
            assertTrue(
                database.unlockDao().isUnlocked("churros", UnlockFeature.PROMO)
            )
            assertNull(database.favoriteDao().get("churros"))
        }

    private fun indexMap(vararg ids: String): Map<String, SearchIndexEntryDto> =
        ids.associateWith { recipeId ->
            when (recipeId) {
                "churros" -> Fixtures.indexEntry("1065-churros-frozen-ide-jualan")
                "martabak" -> Fixtures.indexEntry("67-martabak-tahu-ide-jualan-1000an")
                else -> Fixtures.indexEntry("1065-churros-frozen-ide-jualan")
                    .copy(id = recipeId, slug = recipeId, title = recipeId)
            }
        }

    private fun indexRow(id: String) = RecipeIndexEntity(
        id = id,
        slug = id,
        title = id,
        category = "kue",
        tagsText = "kue",
        ingredientsText = "bahan",
        cover = "https://img.example.com/$id.jpg",
        thumb = null,
        estCapital = 10_000,
        portions = 4,
        isViral = false,
        orderIdx = 0
    )

    private fun favorite(id: String) = FavoriteEntity(
        id = id,
        favoritedAt = 1L,
        title = id,
        category = "kue",
        tagsText = "kue",
        ingredientsText = "bahan",
        estCapital = 10_000,
        portions = 4,
        thumbLocalPath = "/tmp/$id-thumb",
        coverLocalPath = "/tmp/$id-cover",
        detailJson = """{"id":"$id"}"""
    )
}
