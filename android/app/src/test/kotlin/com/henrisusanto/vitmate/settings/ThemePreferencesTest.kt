package com.henrisusanto.vitmate.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ThemePreferencesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `tema default mengikuti sistem`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        val preferences = ThemePreferences(
            PreferenceDataStoreFactory.create(scope = scope) {
                File(tempFolder.root, "settings.preferences_pb")
            }
        )

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode.first())

        scope.coroutineContext[Job]?.cancelAndJoin()
    }

    @Test
    fun `tema tersimpan dan terbaca oleh instance berikutnya`() = runTest {
        val file = File(tempFolder.root, "settings.preferences_pb")

        val firstScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        val first = ThemePreferences(
            PreferenceDataStoreFactory.create(scope = firstScope) { file }
        )
        assertEquals(ThemeMode.SYSTEM, first.themeMode.first())

        first.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, first.themeMode.first())

        firstScope.coroutineContext[Job]?.cancelAndJoin()

        val secondScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        val second = ThemePreferences(
            PreferenceDataStoreFactory.create(scope = secondScope) { file }
        )
        assertEquals(ThemeMode.DARK, second.themeMode.first())

        secondScope.coroutineContext[Job]?.cancelAndJoin()
    }
}
