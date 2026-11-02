package com.henrisusanto.vitmate.timer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val TIMER_DATASTORE_NAME = "timer_notification_counter"

private val Context.timerDataStore: DataStore<Preferences> by
    preferencesDataStore(name = TIMER_DATASTORE_NAME)

/**
 * Penyedia ID notifikasi monotonic. Setiap pemanggilan [nextNotificationId]
 * mengembalikan ID yang lebih besar dari sebelumnya, sehingga ID notifikasi
 * tidak pernah saling menimpa antar timer.
 */
interface NotificationCounter {
    suspend fun nextNotificationId(): Int
}

/**
 * Implementasi [NotificationCounter] yang menyimpan counter di DataStore.
 * Counter disimpan per aplikasi dan bertambah terus, sehingga tidak pernah ulang
 * bahkan setelah aplikasi di-restart.
 */
class TimerPreferences(
    private val dataStore: DataStore<Preferences>
) : NotificationCounter {

    override suspend fun nextNotificationId(): Int {
        var nextValue = 1
        dataStore.edit { preferences ->
            val current = preferences[KEY_NOTIFICATION_COUNTER] ?: 0
            nextValue = if (current >= Int.MAX_VALUE - 1) 1 else current + 1
            preferences[KEY_NOTIFICATION_COUNTER] = nextValue
        }
        return nextValue
    }

    companion object {
        private val KEY_NOTIFICATION_COUNTER = intPreferencesKey("notification_counter")

        fun from(context: Context): TimerPreferences =
            TimerPreferences(context.applicationContext.timerDataStore)
    }
}
