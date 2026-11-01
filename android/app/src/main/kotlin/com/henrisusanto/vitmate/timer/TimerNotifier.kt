package com.henrisusanto.vitmate.timer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.data.repository.ActiveTimer

/**
 * Channel [CHANNEL_ID] dan notifikasi timer. Timer tetap berjalan di dalam app
 * bila izin notifikasi ditolak; di kasus itu [notify] tidak melakukan apa pun.
 */
class TimerNotifier(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            appContext.getString(R.string.timer_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = appContext.getString(R.string.timer_channel_description)
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            enableLights(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null
            )
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun notify(timer: ActiveTimer) {
        if (!canPostNotifications()) return

        val contentIntent = PendingIntent.getActivity(
            appContext,
            timer.notificationId,
            TimerIntents.openRecipe(appContext, timer.recipeId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val message = appContext.getString(
            R.string.timer_notification_content_format,
            timer.label,
            timer.recipeTitle
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(appContext.getString(R.string.timer_notification_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(appContext).notify(timer.notificationId, notification)
        } catch (_: SecurityException) {
            // Izin dicabut tepat sebelum notifikasi tampil; countdown in-app tetap jalan.
        }
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "cooking_timer"
        private val VIBRATION_PATTERN = longArrayOf(0L, 500L, 250L, 500L)
    }
}
