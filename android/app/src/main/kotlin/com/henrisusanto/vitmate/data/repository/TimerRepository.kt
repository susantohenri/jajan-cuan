package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.local.ActiveTimerDao
import com.henrisusanto.vitmate.data.local.ActiveTimerEntity
import com.henrisusanto.vitmate.timer.TimerScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Timer aktif yang dipakai UI; waktu selesai selalu absolut (epoch millis). */
data class ActiveTimer(
    val id: String,
    val recipeId: String,
    val recipeTitle: String,
    val label: String,
    val endAtEpochMillis: Long,
    val notificationId: Int
)

/**
 * Kontrak timer untuk layar Detail. Implementasi menyimpan baris active_timers
 * sekaligus menjadwalkan alarm yang tepat waktu.
 */
interface CookingTimers {
    fun observeActive(): Flow<List<ActiveTimer>>

    /** Mengembalikan null bila durasi tidak valid (kurang dari 1 detik). */
    suspend fun start(
        recipeId: String,
        recipeTitle: String,
        label: String,
        durationSeconds: Long
    ): ActiveTimer?

    suspend fun cancel(timerId: String)

    /** Mengambil timer yang sudah berbunyi sekaligus menghapus barisnya. */
    suspend fun consume(timerId: String): ActiveTimer?
}

class TimerRepository(
    private val dao: ActiveTimerDao,
    private val scheduler: TimerScheduler,
    private val clock: () -> Long = System::currentTimeMillis,
    private val idFactory: () -> String = { UUID.randomUUID().toString() }
) : CookingTimers {

    /** Menjaga agar nomor notifikasi tetap unik walau dua timer dinyalakan bersamaan. */
    private val startMutex = Mutex()

    override fun observeActive(): Flow<List<ActiveTimer>> =
        dao.observeAll().map { timers -> timers.map { it.toModel() } }

    override suspend fun start(
        recipeId: String,
        recipeTitle: String,
        label: String,
        durationSeconds: Long
    ): ActiveTimer? = startMutex.withLock {
        if (durationSeconds < MIN_DURATION_SECONDS) return@withLock null

        val now = clock()
        val id = idFactory()
        val timer = ActiveTimer(
            id = id,
            recipeId = recipeId,
            recipeTitle = recipeTitle,
            label = label,
            endAtEpochMillis = now + durationSeconds * MILLIS_PER_SECOND,
            notificationId = nextNotificationId()
        )
        dao.upsert(timer.toEntity(createdAt = now))
        try {
            scheduler.schedule(timer)
        } catch (exception: RuntimeException) {
            // Jangan tinggalkan timer tanpa alarm.
            dao.delete(id)
            throw exception
        }
        timer
    }

    override suspend fun cancel(timerId: String) {
        val timer = dao.get(timerId)
        if (timer != null) scheduler.cancel(timer.toModel())
        dao.delete(timerId)
    }

    override suspend fun consume(timerId: String): ActiveTimer? {
        val entity = dao.get(timerId) ?: return null
        dao.delete(timerId)
        return entity.toModel()
    }

    /** Membuang baris yang sudah kedaluwarsa, misalnya setelah perangkat reboot. */
    suspend fun pruneExpired() {
        dao.deleteExpired(clock())
    }

    private fun ActiveTimerEntity.toModel() = ActiveTimer(
        id = id,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        label = label,
        endAtEpochMillis = endAtEpochMillis,
        notificationId = notificationId
    )

    private fun ActiveTimer.toEntity(createdAt: Long) = ActiveTimerEntity(
        id = id,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        label = label,
        endAtEpochMillis = endAtEpochMillis,
        createdAt = createdAt,
        notificationId = notificationId
    )

    private suspend fun nextNotificationId(): Int {
        val next = (dao.maxNotificationId() ?: 0) + 1
        return if (next > 0) next else 1
    }

    private companion object {
        const val MIN_DURATION_SECONDS = 1L
        const val MILLIS_PER_SECOND = 1_000L
    }
}
