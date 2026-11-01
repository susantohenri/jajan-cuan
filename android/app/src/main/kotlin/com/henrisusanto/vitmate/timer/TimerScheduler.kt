package com.henrisusanto.vitmate.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.henrisusanto.vitmate.data.repository.ActiveTimer

/**
 * Penjadwal alarm. Implementasi Android memakai [AlarmManager.setAlarmClock]
 * bila platform mengizinkannya. Aplikasi ini sengaja TIDAK meminta
 * SCHEDULE_EXACT_ALARM/USE_EXACT_ALARM, sehingga pada Android 12+ jalur
 * tersebut tidak tersedia dan dipakai [AlarmManager.setAndAllowWhileIdle]
 * sebagai fallback (alarm tetap dibangunkan, waktu bisa sedikit meleset).
 */
interface TimerScheduler {
    fun schedule(timer: ActiveTimer)
    fun cancel(timer: ActiveTimer)
}

class AlarmTimerScheduler(context: Context) : TimerScheduler {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    override fun schedule(timer: ActiveTimer) {
        val operation = requireNotNull(
            broadcastPendingIntent(
                timer = timer,
                flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        if (canScheduleExactAlarms()) {
            try {
                val showIntent = PendingIntent.getActivity(
                    appContext,
                    timer.notificationId,
                    TimerIntents.openRecipe(appContext, timer.recipeId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(timer.endAtEpochMillis, showIntent),
                    operation
                )
                return
            } catch (_: SecurityException) {
                // Izin exact alarm bisa dicabut kapan saja; pakai fallback inexact.
            }
        }
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timer.endAtEpochMillis,
            operation
        )
    }

    override fun cancel(timer: ActiveTimer) {
        val operation = broadcastPendingIntent(
            timer = timer,
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(operation)
        operation.cancel()
    }

    private fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun broadcastPendingIntent(timer: ActiveTimer, flags: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            appContext,
            timer.notificationId,
            Intent(appContext, TimerReceiver::class.java).apply {
                action = TimerReceiver.ACTION_TIMER_FIRED
                putExtra(TimerReceiver.EXTRA_TIMER_ID, timer.id)
            },
            flags
        )
}
