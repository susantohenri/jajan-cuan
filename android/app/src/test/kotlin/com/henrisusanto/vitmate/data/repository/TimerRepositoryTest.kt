package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.local.ActiveTimerDao
import com.henrisusanto.vitmate.data.local.ActiveTimerEntity
import com.henrisusanto.vitmate.timer.NotificationCounter
import com.henrisusanto.vitmate.timer.TimerScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerRepositoryTest {

    private val dao = FakeActiveTimerDao()
    private val scheduler = RecordingTimerScheduler()
    private val ids = ArrayDeque(listOf("timer-a", "timer-b", "timer-c"))
    private val preferences = FakeTimerPreferences()

    private fun repository(nowMillis: Long = 1_000L) = TimerRepository(
        dao = dao,
        scheduler = scheduler,
        notificationCounter = preferences,
        clock = { nowMillis },
        idFactory = { ids.removeFirst() }
    )

    @Test
    fun `start menyimpan waktu selesai absolut dan menjadwalkan alarm`() = runBlocking {
        val repository = repository(nowMillis = 1_000L)

        val timer = repository.start(
            recipeId = "resep-1",
            recipeTitle = "Martabak Tahu",
            label = "Goreng 5 menit",
            durationSeconds = 300L
        )

        assertNotNull(timer)
        assertEquals(301_000L, timer?.endAtEpochMillis)
        assertEquals(1_000L, dao.rows.value.single().createdAt)
        assertEquals("Martabak Tahu", dao.rows.value.single().recipeTitle)
        assertEquals(listOf(timer), scheduler.scheduled)
        assertTrue((timer?.notificationId ?: 0) > 0)
    }

    @Test
    fun `beberapa timer aktif memakai id dan notification id berbeda`() = runBlocking {
        val repository = repository()

        val first = repository.start("resep-1", "Resep Satu", "Langkah satu", 60L)
        val second = repository.start("resep-2", "Resep Dua", "Langkah dua", 120L)

        assertNotNull(first)
        assertNotNull(second)
        assertNotEquals(first?.id, second?.id)
        assertNotEquals(first?.notificationId, second?.notificationId)
        assertEquals(2, dao.rows.value.size)
        assertEquals(2, scheduler.scheduled.size)
        assertEquals(
            listOf("timer-a", "timer-b"),
            repository.observeActive().first().map(ActiveTimer::id)
        )
    }

    @Test
    fun `cancel menghapus baris dan membatalkan alarm`() = runBlocking {
        val repository = repository()
        val timer = repository.start("resep-1", "Resep Satu", "Langkah satu", 60L)!!

        repository.cancel(timer.id)

        assertTrue(dao.rows.value.isEmpty())
        assertEquals(listOf(timer), scheduler.cancelled)
    }

    @Test
    fun `consume hanya menghapus timer yang diminta`() = runBlocking {
        val repository = repository()
        val first = repository.start("resep-1", "Resep Satu", "Langkah satu", 60L)!!
        val second = repository.start("resep-1", "Resep Satu", "Langkah dua", 120L)!!

        val consumed = repository.consume(first.id)

        assertEquals(first, consumed)
        assertNull(repository.consume("tidak-ada"))
        assertEquals(listOf(second.id), dao.rows.value.map(ActiveTimerEntity::id))
    }

    @Test
    fun `durasi kurang dari satu detik tidak membuat timer`() = runBlocking {
        val repository = repository()

        assertNull(repository.start("resep-1", "Resep Satu", "Langkah", 0L))
        assertTrue(dao.rows.value.isEmpty())
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `baris dihapus bila penjadwalan alarm gagal`() = runBlocking {
        scheduler.failSchedule = true

        val result = runCatching {
            repository().start("resep-1", "Resep Satu", "Langkah", 60L)
        }

        assertTrue(result.isFailure)
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test
    fun `prune menghapus timer yang sudah lewat`() = runBlocking {
        val started = repository(nowMillis = 1_000L)
            .start("resep-1", "Resep Satu", "Langkah", 60L)!!
        assertEquals(1, dao.rows.value.size)

        TimerRepository(
            dao = dao,
            scheduler = scheduler,
            notificationCounter = preferences,
            clock = { started.endAtEpochMillis },
            idFactory = { "timer-z" }
        ).pruneExpired()

        assertTrue(dao.rows.value.isEmpty())
    }

    private class FakeActiveTimerDao : ActiveTimerDao {
        val rows = MutableStateFlow<List<ActiveTimerEntity>>(emptyList())

        override fun observeAll(): Flow<List<ActiveTimerEntity>> =
            rows.map { timers -> timers.sortedBy(ActiveTimerEntity::endAtEpochMillis) }

        override suspend fun getAll(): List<ActiveTimerEntity> =
            rows.value.sortedBy(ActiveTimerEntity::endAtEpochMillis)

        override suspend fun get(timerId: String): ActiveTimerEntity? =
            rows.value.firstOrNull { it.id == timerId }

        override suspend fun maxNotificationId(): Int? =
            rows.value.maxOfOrNull(ActiveTimerEntity::notificationId)

        override suspend fun upsert(timer: ActiveTimerEntity) {
            rows.update { timers -> timers.filterNot { it.id == timer.id } + timer }
        }

        override suspend fun delete(timerId: String) {
            rows.update { timers -> timers.filterNot { it.id == timerId } }
        }

        override suspend fun deleteExpired(nowMillis: Long) {
            rows.update { timers -> timers.filter { it.endAtEpochMillis > nowMillis } }
        }
    }

    private class FakeTimerPreferences : NotificationCounter {
        private var counter = 0

        override suspend fun nextNotificationId(): Int {
            counter++
            return counter
        }
    }

    private class RecordingTimerScheduler : TimerScheduler {
        val scheduled = mutableListOf<ActiveTimer>()
        val cancelled = mutableListOf<ActiveTimer>()
        var failSchedule: Boolean = false

        override fun schedule(timer: ActiveTimer) {
            if (failSchedule) throw IllegalStateException("alarm gagal dijadwalkan")
            scheduled += timer
        }

        override fun cancel(timer: ActiveTimer) {
            cancelled += timer
        }
    }
}
