package com.henrisusanto.vitmate.data.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.henrisusanto.vitmate.config.RemoteConfigValues
import com.henrisusanto.vitmate.data.local.JajanCuanDatabase
import com.henrisusanto.vitmate.data.model.UnlockFeature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class UnlockRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: JajanCuanDatabase
    private lateinit var sessionUnlocks: SessionUnlocks
    private lateinit var repository: UnlockRepository

    private val config = MutableStateFlow(RemoteConfigValues.DEFAULTS)

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        database = newDatabase()
        sessionUnlocks = SessionUnlocks()
        repository = newRepository(database)
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
    }

    @Test
    fun `tanpa baris dan tanpa sesi semua fitur terkunci`() = runBlocking {
        assertFalse(repository.isUnlocked(RECIPE_A, UnlockFeature.CALCULATOR))
        assertFalse(repository.isUnlocked(RECIPE_A, UnlockFeature.PROMO))
        assertTrue(repository.observeUnlockedFeatures(RECIPE_A).first().isEmpty())
    }

    @Test
    fun `iklan dimatikan membuka semua fitur tanpa menulis baris`() = runBlocking {
        config.value = RemoteConfigValues.DEFAULTS.copy(isAdsEnabled = false)

        assertTrue(repository.isUnlocked(RECIPE_A, UnlockFeature.CALCULATOR))
        assertTrue(repository.isUnlocked(RECIPE_A, UnlockFeature.PROMO))
        assertEquals(
            UnlockFeature.entries.toSet(),
            repository.observeUnlockedFeatures(RECIPE_A).first()
        )
        assertEquals(0, database.unlockDao().count())
    }

    @Test
    fun `rewarded dimatikan membuka semua fitur tanpa menulis baris`() = runBlocking {
        config.value = RemoteConfigValues.DEFAULTS.copy(rewardedEnabled = false)

        assertTrue(repository.isUnlocked(RECIPE_A, UnlockFeature.CALCULATOR))
        assertTrue(repository.isUnlocked(RECIPE_A, UnlockFeature.PROMO))
        assertEquals(0, database.unlockDao().count())
    }

    @Test
    fun `unlock permanen hanya untuk resep dan fitur tersebut`() = runBlocking {
        repository.persistUnlock(RECIPE_A, UnlockFeature.CALCULATOR)

        assertTrue(repository.isUnlocked(RECIPE_A, UnlockFeature.CALCULATOR))
        assertFalse(repository.isUnlocked(RECIPE_A, UnlockFeature.PROMO))
        assertFalse(repository.isUnlocked(RECIPE_B, UnlockFeature.CALCULATOR))
        assertEquals(
            setOf(UnlockFeature.CALCULATOR),
            repository.observeUnlockedFeatures(RECIPE_A).first()
        )
        assertEquals(
            123L,
            database.unlockDao().get(RECIPE_A, UnlockFeature.CALCULATOR)?.unlockedAt
        )
    }

    @Test
    fun `unlock sesi tidak menulis ke tabel unlocks`() = runBlocking {
        repository.unlockForSession(RECIPE_A, UnlockFeature.PROMO)

        assertTrue(repository.isUnlocked(RECIPE_A, UnlockFeature.PROMO))
        assertFalse(repository.isUnlocked(RECIPE_B, UnlockFeature.PROMO))
        assertEquals(
            setOf(UnlockFeature.PROMO),
            repository.observeUnlockedFeatures(RECIPE_A).first()
        )
        assertEquals(0, database.unlockDao().count())
    }

    @Test
    fun `perubahan remote config langsung terlihat oleh flow`() = runBlocking {
        val before = repository.observeUnlockedFeatures(RECIPE_A).first()
        assertTrue(before.isEmpty())

        config.value = RemoteConfigValues.DEFAULTS.copy(rewardedEnabled = false)

        assertEquals(
            UnlockFeature.entries.toSet(),
            repository.observeUnlockedFeatures(RECIPE_A).first()
        )
        assertEquals(0, database.unlockDao().count())
    }

    @Test
    fun `unlock permanen bertahan setelah database dibuka ulang`() = runBlocking {
        val databaseName = "unlock-persist-${System.nanoTime()}.db"
        val firstDatabase = Room.databaseBuilder(
            context,
            JajanCuanDatabase::class.java,
            databaseName
        ).allowMainThreadQueries().build()
        try {
            newRepository(firstDatabase)
                .persistUnlock(RECIPE_A, UnlockFeature.CALCULATOR)
        } finally {
            firstDatabase.close()
        }

        val secondDatabase = Room.databaseBuilder(
            context,
            JajanCuanDatabase::class.java,
            databaseName
        ).allowMainThreadQueries().build()
        try {
            val reopened = newRepository(secondDatabase)
            assertTrue(reopened.isUnlocked(RECIPE_A, UnlockFeature.CALCULATOR))
            assertFalse(reopened.isUnlocked(RECIPE_A, UnlockFeature.PROMO))
            assertEquals(1, secondDatabase.unlockDao().count())
        } finally {
            secondDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun newDatabase(): JajanCuanDatabase =
        Room.inMemoryDatabaseBuilder(context, JajanCuanDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private fun newRepository(database: JajanCuanDatabase): UnlockRepository =
        UnlockRepository(
            dao = database.unlockDao(),
            sessionUnlocks = sessionUnlocks,
            config = config,
            clock = { 123L }
        )

    private companion object {
        const val RECIPE_A = "resep-a"
        const val RECIPE_B = "resep-b"
    }
}
