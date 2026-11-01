package com.henrisusanto.vitmate.timer

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.henrisusanto.vitmate.data.repository.ActiveTimer
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@Suppress("DEPRECATION")
class AlarmTimerSchedulerTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var scheduler: AlarmTimerScheduler

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        alarmManager = context.getSystemService(AlarmManager::class.java)
        scheduler = AlarmTimerScheduler(context)
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
    }

    @Test
    fun `schedule memakai setAlarmClock bila izin exact alarm tersedia`() {
        val timer = timer()

        scheduler.schedule(timer)

        val alarm = shadowOf(alarmManager).scheduledAlarms.single()
        assertEquals(AlarmManager.RTC_WAKEUP, alarm.getType())
        assertEquals(timer.endAtEpochMillis, alarm.getTriggerAtMs())
        assertNotNull(alarm.getAlarmClockInfo())

        val operation = shadowOf(alarm.operation)
        assertTrue(operation.isImmutable)
        assertEquals(TimerReceiver.ACTION_TIMER_FIRED, operation.savedIntent.action)
        assertEquals(
            timer.id,
            operation.savedIntent.getStringExtra(TimerReceiver.EXTRA_TIMER_ID)
        )

        val showIntent = shadowOf(alarm.showIntent)
        assertNotNull(showIntent)
        assertTrue(showIntent.isImmutable)
        assertEquals(
            timer.recipeId,
            showIntent.savedIntent.getStringExtra(TimerIntents.EXTRA_RECIPE_ID)
        )
    }

    @Test
    fun `tanpa izin exact alarm memakai alarm inexact yang tetap membangunkan perangkat`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val timer = timer()

        scheduler.schedule(timer)

        val alarm = shadowOf(alarmManager).scheduledAlarms.single()
        assertEquals(AlarmManager.RTC_WAKEUP, alarm.getType())
        assertEquals(timer.endAtEpochMillis, alarm.getTriggerAtMs())
        assertNull(alarm.getAlarmClockInfo())
        assertTrue(alarm.isAllowWhileIdle)

        val operation = shadowOf(alarm.operation)
        assertTrue(operation.isImmutable)
        assertEquals(TimerReceiver.ACTION_TIMER_FIRED, operation.savedIntent.action)
        assertEquals(
            timer.id,
            operation.savedIntent.getStringExtra(TimerReceiver.EXTRA_TIMER_ID)
        )
    }

    @Test
    fun `cancel menghapus alarm yang sudah dijadwalkan`() {
        val timer = timer()
        scheduler.schedule(timer)
        assertEquals(1, shadowOf(alarmManager).scheduledAlarms.size)

        scheduler.cancel(timer)

        assertTrue(shadowOf(alarmManager).scheduledAlarms.isEmpty())
    }

    @Test
    fun `manifest tidak meminta izin exact alarm dan tetap meminta izin notifikasi`() {
        val requested = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            ?.toList()
            .orEmpty()

        assertTrue(requested.contains(Manifest.permission.POST_NOTIFICATIONS))
        assertFalse(requested.contains(Manifest.permission.SCHEDULE_EXACT_ALARM))
        assertFalse(requested.contains(Manifest.permission.USE_EXACT_ALARM))
    }

    private fun timer(
        id: String = "timer-1",
        endAtEpochMillis: Long = 100_000L
    ) = ActiveTimer(
        id = id,
        recipeId = "resep-1",
        recipeTitle = "Martabak Tahu",
        label = "Goreng 5 menit",
        endAtEpochMillis = endAtEpochMillis,
        notificationId = 42
    )
}
