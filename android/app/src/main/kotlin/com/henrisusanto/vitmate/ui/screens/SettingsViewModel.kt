package com.henrisusanto.vitmate.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.settings.ThemeMode
import com.henrisusanto.vitmate.settings.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val versionName: String = "",
    val showAdsPrivacyOptions: Boolean = false
)

/**
 * Menggabungkan preferensi tema tersimpan dengan status UMP. Tombol pengaturan
 * privasi iklan hanya tampil ketika UMP menyatakan status REQUIRED.
 */
class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val analytics: AnalyticsLogger,
    privacyOptionsRequired: StateFlow<Boolean>,
    versionName: String
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        themePreferences.themeMode,
        privacyOptionsRequired
    ) { themeMode, adsPrivacyRequired ->
        SettingsUiState(
            themeMode = themeMode,
            versionName = versionName,
            showAdsPrivacyOptions = adsPrivacyRequired
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(versionName = versionName)
    )

    /**
     * Menyimpan pilihan lalu mencatat `change_theme`. Pilihan yang sama dengan
     * nilai tersimpan tidak memicu event maupun penulisan DataStore.
     */
    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            val current = themePreferences.themeMode.first()
            if (current == mode) return@launch
            themePreferences.setThemeMode(mode)
            analytics.changeTheme(mode.analyticsValue)
        }
    }
}
