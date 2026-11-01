package com.henrisusanto.vitmate.timer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.edit

/**
 * Mengingat apakah dialog izin POST_NOTIFICATIONS sudah pernah diminta, agar
 * permintaan hanya muncul saat timer pertama dinyalakan.
 */
class TimerPermissionPrompt(context: Context) {

    private val appContext = context.applicationContext
    private val preferences =
        appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    fun shouldRequest(): Boolean =
        !isGranted() && !preferences.getBoolean(KEY_REQUESTED, false)

    fun markRequested() {
        preferences.edit { putBoolean(KEY_REQUESTED, true) }
    }

    private companion object {
        const val PREFERENCES_NAME = "timer_permission"
        const val KEY_REQUESTED = "notification_permission_requested"
    }
}
