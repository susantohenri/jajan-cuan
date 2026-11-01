package com.henrisusanto.vitmate.settings

/**
 * Pilihan tema yang disimpan di DataStore dan dipakai untuk user property
 * `theme_mode` (system/light/dark).
 */
enum class ThemeMode(val storageValue: String, val analyticsValue: String) {
    SYSTEM("system", "system"),
    LIGHT("light", "light"),
    DARK("dark", "dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}
