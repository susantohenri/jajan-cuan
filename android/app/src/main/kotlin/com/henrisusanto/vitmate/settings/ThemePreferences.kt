package com.henrisusanto.vitmate.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SETTINGS_DATASTORE_NAME = "settings"

private val Context.settingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = SETTINGS_DATASTORE_NAME)

/**
 * Sumber tunggal preferensi tampilan. Nilai default adalah [ThemeMode.SYSTEM]
 * bila DataStore masih kosong atau tidak bisa dibaca.
 */
class ThemePreferences(private val dataStore: DataStore<Preferences>) {

    val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> ThemeMode.fromStorage(preferences[KEY_THEME_MODE]) }
        .distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.storageValue
        }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

        fun from(context: Context): ThemePreferences =
            ThemePreferences(context.applicationContext.settingsDataStore)
    }
}
