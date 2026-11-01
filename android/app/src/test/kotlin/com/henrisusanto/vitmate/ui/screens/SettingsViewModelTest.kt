package com.henrisusanto.vitmate.ui.screens

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.settings.ThemeMode
import com.henrisusanto.vitmate.settings.ThemePreferences
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import com.henrisusanto.vitmate.ui.testutil.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var sink: RecordingAnalyticsSink
    private lateinit var privacyOptionsRequired: MutableStateFlow<Boolean>
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var themePreferences: ThemePreferences

    @After
    fun tearDown() {
        dataStoreScope.coroutineContext[Job]?.cancel()
    }

    private fun createViewModel(): SettingsViewModel {
        sink = RecordingAnalyticsSink()
        privacyOptionsRequired = MutableStateFlow(false)
        dataStoreScope = CoroutineScope(UnconfinedTestDispatcher() + Job())
        themePreferences = ThemePreferences(
            PreferenceDataStoreFactory.create(scope = dataStoreScope) {
                File(tempFolder.root, "settings.preferences_pb")
            }
        )
        return SettingsViewModel(
            themePreferences = themePreferences,
            analytics = AnalyticsLogger(
                analytics = sink,
                crashlytics = RecordingCrashlyticsSink(),
                installTimeMillis = 0L,
                nowMillis = { 1L }
            ),
            privacyOptionsRequired = privacyOptionsRequired,
            versionName = "1.0.0"
        )
    }

    @Test
    fun `tombol privasi iklan hanya tampil saat status REQUIRED`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)

        assertFalse(viewModel.uiState.value.showAdsPrivacyOptions)

        privacyOptionsRequired.value = true
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showAdsPrivacyOptions)

        privacyOptionsRequired.value = false
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showAdsPrivacyOptions)
    }

    @Test
    fun `memilih tema menyimpan dan mencatat change_theme`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)

        viewModel.onThemeSelected(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, themePreferences.themeMode.first())
        assertTrue(
            sink.events.any {
                it.name == "change_theme" && it.params["theme_mode"] == "dark"
            }
        )
        assertEquals("dark", sink.userProperties["theme_mode"])
    }

    @Test
    fun `memilih tema yang sama tidak mencatat change_theme`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)

        viewModel.onThemeSelected(ThemeMode.SYSTEM)
        advanceUntilIdle()

        assertTrue(sink.events.none { it.name == "change_theme" })
        assertEquals(ThemeMode.SYSTEM, themePreferences.themeMode.first())
    }

    private fun TestScope.collectState(viewModel: SettingsViewModel) {
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()
    }
}
