package com.henrisusanto.vitmate.ui.screens

import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import com.henrisusanto.vitmate.ui.testutil.FakeRecipeCatalog
import com.henrisusanto.vitmate.ui.testutil.MainDispatcherRule
import com.henrisusanto.vitmate.ui.testutil.sampleRecipes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sink: RecordingAnalyticsSink
    private lateinit var catalog: FakeRecipeCatalog
    private lateinit var viewModel: FavoritesViewModel
    private val collectedEvents = mutableListOf<FavoriteUiEvent>()

    @Before
    fun setUp() {
        sink = RecordingAnalyticsSink()
        val favorites = sampleRecipes().take(3).map { it.copy(isFavorite = true) }
        catalog = FakeRecipeCatalog(favorites)
        viewModel = FavoritesViewModel(
            repository = catalog,
            analytics = AnalyticsLogger(
                analytics = sink,
                crashlytics = RecordingCrashlyticsSink(),
                installTimeMillis = 0L,
                nowMillis = { 1L }
            )
        )
    }

    @Test
    fun `view favorites tab dicatat sekali dengan jumlah favorit`() = runTest {
        val events = sink.events.filter { it.name == "view_favorites_tab" }
        assertEquals(1, events.size)
        assertEquals(3, events.single().params["total_favorites_count"])
    }

    @Test
    fun `pencarian lokal menyaring snapshot favorit`() = runTest {
        collectState()

        assertEquals(3, viewModel.state.value.recipes.size)

        viewModel.onQueryChange("Resep 2")
        advance(FavoritesViewModel.SEARCH_DEBOUNCE_MILLIS + 1)

        assertEquals("Resep 2", catalog.lastFavoriteQuery)
        assertEquals(
            listOf("recipe-2"),
            viewModel.state.value.recipes.map { it.id }
        )
    }

    @Test
    fun `hapus favorit memperbarui daftar dan mencatat event`() = runTest {
        collectState()
        val target = viewModel.state.value.recipes.first()

        viewModel.onRemoveFavorite(target.id)
        advance()

        assertEquals(2, viewModel.state.value.totalCount)
        assertTrue(viewModel.state.value.recipes.none { it.id == target.id })
        assertTrue(
            sink.events.any {
                it.name == "recipe_remove_favorite" && it.params["recipe_id"] == target.id
            }
        )
        assertTrue(collectedEvents.contains(FavoriteUiEvent.Removed))
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.state.collect { }
        }
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.events.collect { collectedEvents += it }
        }
        advance()
    }

    private fun advance(millis: Long = 0) {
        if (millis > 0) {
            mainDispatcherRule.dispatcher.scheduler.advanceTimeBy(millis)
        }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    }
}
