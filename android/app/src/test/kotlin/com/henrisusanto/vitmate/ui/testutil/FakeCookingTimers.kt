package com.henrisusanto.vitmate.ui.testutil

import com.henrisusanto.vitmate.data.repository.ActiveTimer
import com.henrisusanto.vitmate.data.repository.CookingTimers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Timer palsu untuk unit test ViewModel; tidak menyentuh AlarmManager/Room. */
class FakeCookingTimers : CookingTimers {

    data class Started(
        val recipeId: String,
        val recipeTitle: String,
        val label: String,
        val durationSeconds: Long
    )

    val active = MutableStateFlow<List<ActiveTimer>>(emptyList())
    val started = mutableListOf<Started>()
    val cancelled = mutableListOf<String>()
    var failStart: Boolean = false

    override fun observeActive(): Flow<List<ActiveTimer>> = active

    override suspend fun start(
        recipeId: String,
        recipeTitle: String,
        label: String,
        durationSeconds: Long
    ): ActiveTimer? {
        if (failStart || durationSeconds < 1L) return null
        val index = started.size + 1
        val timer = ActiveTimer(
            id = "timer-$index",
            recipeId = recipeId,
            recipeTitle = recipeTitle,
            label = label,
            endAtEpochMillis = 1_000_000L + durationSeconds * 1_000L,
            notificationId = index
        )
        started += Started(recipeId, recipeTitle, label, durationSeconds)
        active.update { (it + timer).sortedBy(ActiveTimer::endAtEpochMillis) }
        return timer
    }

    override suspend fun cancel(timerId: String) {
        cancelled += timerId
        active.update { timers -> timers.filterNot { it.id == timerId } }
    }

    override suspend fun consume(timerId: String): ActiveTimer? {
        val timer = active.value.firstOrNull { it.id == timerId } ?: return null
        active.update { timers -> timers.filterNot { it.id == timerId } }
        return timer
    }
}
