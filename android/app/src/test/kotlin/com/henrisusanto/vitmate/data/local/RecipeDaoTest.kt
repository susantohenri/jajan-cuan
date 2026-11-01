package com.henrisusanto.vitmate.data.local

import android.app.Application
import androidx.room.Room
import com.henrisusanto.vitmate.data.model.UnlockFeature
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class RecipeDaoTest {

    private lateinit var database: JajanCuanDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            JajanCuanDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `pencarian mencakup judul bahan tag dan kategori`() = runBlocking {
        insertCatalog()

        assertEquals(
            listOf("1065-churros-frozen-ide-jualan"),
            search("churros").map { it.id }
        )
        assertEquals(
            listOf("1065-churros-frozen-ide-jualan"),
            search("terigu").map { it.id }
        )
        assertEquals(
            listOf("1065-churros-frozen-ide-jualan"),
            search("viral").map { it.id }
        )
        assertEquals(
            listOf("67-martabak-tahu-ide-jualan-1000an"),
            search("gorengan").map { it.id }
        )
    }

    @Test
    fun `filter kategori digabung dengan kata kunci`() = runBlocking {
        insertCatalog()
        database.recipeIndexDao().insertAll(
            listOf(
                row("mini-kue", title = "Mini Kue", category = "kue", order = 10)
            )
        )

        assertEquals(
            listOf("mini-kue"),
            search("mini", category = "kue").map { it.id }
        )
        assertEquals(
            listOf("67-martabak-tahu-ide-jualan-1000an"),
            search("mini", category = "gorengan").map { it.id }
        )
    }

    @Test
    fun `limit membatasi jumlah baris`() = runBlocking {
        repeat(5) { index ->
            database.recipeIndexDao().insertAll(
                listOf(row("resep-$index", title = "Resep $index", order = index))
            )
        }

        assertEquals(2, search("", limit = 2).size)
        assertEquals(5, search("", limit = 10).size)
    }

    @Test
    fun `persen dan garis bawah dicari sebagai literal`() = runBlocking {
        database.recipeIndexDao().insertAll(
            listOf(
                row("diskon", title = "Diskon 100% Spesial", order = 0),
                row("rahasia", title = "resep_rahasia", order = 1),
                row("biasa", title = "Resep Biasa", order = 2)
            )
        )

        assertEquals(listOf("diskon"), search("100%").map { it.id })
        assertEquals(listOf("diskon"), search("%").map { it.id })
        assertEquals(listOf("rahasia"), search("_").map { it.id })
    }

    @Test
    fun `kategori distinct diurutkan`() = runBlocking {
        insertCatalog()

        assertEquals(
            listOf("gorengan", "kue", "minuman"),
            database.recipeIndexDao().observeCategories().first()
        )
    }

    @Test
    fun `cache detail hanya menyisakan 30 terbaru`() = runBlocking {
        repeat(31) { index ->
            database.recipeDetailDao().upsert(
                RecipeDetailEntity(
                    id = "detail-$index",
                    json = """{"id":"detail-$index"}""",
                    fetchedAt = index.toLong()
                )
            )
        }

        database.recipeDetailDao().pruneTo(30)

        assertEquals(30, database.recipeDetailDao().count())
        assertNull(database.recipeDetailDao().get("detail-0"))
        assertNotNull(database.recipeDetailDao().get("detail-30"))
    }

    @Test
    fun `unlock idempoten dan tidak menimpa waktu pertama`() = runBlocking {
        val dao = database.unlockDao()
        dao.insert(UnlockEntity("resep-a", UnlockFeature.CALCULATOR, 100L))
        dao.insert(UnlockEntity("resep-a", UnlockFeature.CALCULATOR, 200L))

        assertEquals(1, dao.count())
        assertTrue(dao.isUnlocked("resep-a", UnlockFeature.CALCULATOR))
        assertFalse(dao.isUnlocked("resep-a", UnlockFeature.PROMO))
        assertEquals(100L, dao.get("resep-a", UnlockFeature.CALCULATOR)?.unlockedAt)
    }

    @Test
    fun `pencarian favorit memakai snapshot lokal`() = runBlocking {
        val dao = database.favoriteDao()
        dao.upsert(favorite("a", "Churros", "kue", "terigu", 200L))
        dao.upsert(favorite("b", "Es Bongko", "minuman", "santan", 100L))

        assertEquals(
            listOf("a"),
            dao.observeFiltered("churros").first().map { it.id }
        )
        assertEquals(
            listOf("a", "b"),
            dao.observeAll().first().map { it.id }
        )
        assertEquals(
            listOf("b"),
            dao.observeFiltered("santan").first().map { it.id }
        )
    }

    @Test
    fun `calc state dan timer tersimpan`() = runBlocking {
        database.calcStateDao().upsert(
            CalcStateEntity("resep-a", "[1000]", 4, "PERCENT", 100.0)
        )
        assertEquals(4, database.calcStateDao().get("resep-a")?.portions)

        val timerDao = database.activeTimerDao()
        timerDao.upsert(timer("t1", 500L))
        timerDao.upsert(timer("t2", 2_000L))
        timerDao.deleteExpired(nowMillis = 1_000L)

        assertEquals(listOf("t2"), timerDao.getAll().map { it.id })
    }

    private suspend fun insertCatalog() {
        database.recipeIndexDao().insertAll(
            listOf(
                row(
                    id = "1065-churros-frozen-ide-jualan",
                    title = "Churros Frozen Crispy",
                    category = "kue",
                    ingredients = "terigu telur margarin",
                    tags = "kue modal-kecil viral",
                    order = 0
                ),
                row(
                    id = "67-martabak-tahu-ide-jualan-1000an",
                    title = "Martabak Tahu Mini Crispy",
                    category = "gorengan",
                    ingredients = "tahu telur daun bawang",
                    tags = "gorengan modal-kecil",
                    order = 1
                ),
                row(
                    id = "271-es-bongko-khas-pontianak-bisa-untuk-takjil-ide-jualan",
                    title = "Es Bongko Khas Pontianak",
                    category = "minuman",
                    ingredients = "tepung hunkwe santan",
                    tags = "minuman",
                    order = 2
                )
            )
        )
    }

    private suspend fun search(
        query: String,
        category: String? = null,
        limit: Int = 10
    ) = database.recipeIndexDao()
        .observeList(category, escapeLikePattern(query), limit)
        .first()

    private fun row(
        id: String,
        title: String = "Judul $id",
        category: String = "kue",
        ingredients: String = "bahan",
        tags: String = category,
        order: Int = 0
    ) = RecipeIndexEntity(
        id = id,
        slug = id,
        title = title,
        category = category,
        tagsText = tags,
        ingredientsText = ingredients,
        cover = "https://img.example.com/$id.jpg",
        thumb = "img/recipes/thumbs/$id.webp",
        estCapital = 10_000,
        portions = 4,
        isViral = tags.contains("viral"),
        orderIdx = order
    )

    private fun favorite(
        id: String,
        title: String,
        category: String,
        ingredients: String,
        favoritedAt: Long
    ) = FavoriteEntity(
        id = id,
        favoritedAt = favoritedAt,
        title = title,
        category = category,
        tagsText = category,
        ingredientsText = ingredients,
        estCapital = 10_000,
        portions = 4,
        thumbLocalPath = "/tmp/$id-thumb",
        coverLocalPath = "/tmp/$id-cover",
        detailJson = """{"id":"$id"}"""
    )

    private fun timer(id: String, endAt: Long) = ActiveTimerEntity(
        id = id,
        recipeId = "resep-a",
        recipeTitle = "Resep A",
        label = "Rebus",
        endAtEpochMillis = endAt,
        createdAt = 0L,
        notificationId = id.hashCode()
    )
}
