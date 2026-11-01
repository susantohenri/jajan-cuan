package com.henrisusanto.vitmate.timer

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import com.henrisusanto.vitmate.data.repository.ActiveTimer
import org.junit.Assert.assertEquals
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TimerNotifierTest {

    private lateinit var application: Application
    private lateinit var manager: NotificationManager
    private lateinit var notifier: TimerNotifier

    @Before
    fun setUp() {
        application = RuntimeEnvironment.getApplication()
        shadowOf(application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        manager = application.getSystemService(NotificationManager::class.java)
        notifier = TimerNotifier(application)
    }

    @Test
    fun `channel cooking timer memakai importance high suara dan getar`() {
        notifier.ensureChannel()

        val channel = manager.getNotificationChannel(TimerNotifier.CHANNEL_ID)

        assertNotNull(channel)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertTrue(channel.shouldVibrate())
        assertNotNull(channel.sound)
    }

    @Test
    fun `notifikasi berisi label timer dan nama resep serta membuka resep`() {
        notifier.ensureChannel()
        val timer = timer()

        notifier.notify(timer)

        val notification = shadowOf(manager).getNotification(timer.notificationId)
        assertNotNull(notification)
        assertEquals(
            "Waktu Memasak Selesai",
            notification.extras.getString(Notification.EXTRA_TITLE)
        )
        val text = notification.extras.getString(Notification.EXTRA_TEXT).orEmpty()
        assertTrue(text.contains("Goreng 5 menit"))
        assertTrue(text.contains("Martabak Tahu"))

        val contentIntent = shadowOf(notification.contentIntent)
        assertTrue(contentIntent.isImmutable)
        assertEquals(
            timer.recipeId,
            contentIntent.savedIntent.getStringExtra(TimerIntents.EXTRA_RECIPE_ID)
        )
    }

    @Test
    fun `tanpa izin notifikasi timer tidak diposting`() {
        shadowOf(application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        notifier.ensureChannel()
        val timer = timer()

        notifier.notify(timer)

        assertNull(shadowOf(manager).getNotification(timer.notificationId))
    }

    private fun timer() = ActiveTimer(
        id = "timer-1",
        recipeId = "resep-1",
        recipeTitle = "Martabak Tahu",
        label = "Goreng 5 menit",
        endAtEpochMillis = 100_000L,
        notificationId = 42
    )
}
