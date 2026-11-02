package com.henrisusanto.vitmate.ui.screens

import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import com.henrisusanto.vitmate.ui.testutil.FakeRecipeCatalog
import com.henrisusanto.vitmate.ui.testutil.MainDispatcherRule
import com.henrisusanto.vitmate.ui.testutil.sampleRecipes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sink: RecordingAnalyticsSink
    private lateinit var catalog: FakeRecipeCatalog
    private lateinit var viewModel: HomeViewModel
    private var nextSeed = 42L

    @Before
    fun setUp() {
        sink = RecordingAnalyticsSink()
        catalog = FakeRecipeCatalog(sampleRecipes())
        nextSeed = 42L
        viewModel = HomeViewModel(
            repository = catalog,
            analytics = AnalyticsLogger(
                analytics = sink,
                crashlytics = RecordingCrashlyticsSink(),
                installTimeMillis = 0L,
                nowMillis = { 1L }
            ),
            pageSize = MutableStateFlow(10),
            refresh = { true },
            sessionSeedFactory = { nextSeed }
        )
    }

    @Test
    fun `paginasi menambah limit dan reset saat filter berubah`() = runTest {
        collectState()

        assertEquals(10, catalog.lastLimit)
        assertEquals(10, viewModel.state.value.recipes.size)
        assertEquals(20, viewModel.state.value.totalMatched)
        assertEquals(42L, catalog.lastSessionSeed)

        viewModel.onLoadMore()
        advance()
        assertEquals(20, catalog.lastLimit)
        assertEquals(20, viewModel.state.value.recipes.size)

        viewModel.onCategorySelected("Kue")
        advance()
        assertEquals(10, catalog.lastLimit)
        assertEquals("Kue", catalog.lastCategory)
        assertEquals(10, viewModel.state.value.totalMatched)

        viewModel.onQueryChange("tapioka")
        advance(HomeViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
        assertEquals("tapioka", catalog.lastQuery)
        assertEquals(10, catalog.lastLimit)
        // Saat ada kata kunci, pengacakan sesi tidak dipakai.
        assertNull(catalog.lastSessionSeed)
    }

    @Test
    fun `pencarian di-debounce dan dicatat sekali per term final`() = runTest {
        collectState()

        viewModel.onQueryChange("ta")
        // Hanya majukan waktu, jangan drain sampai idle: debounce berikutnya
        // harus dibatalkan oleh ketikan baru.
        mainDispatcherRule.dispatcher.scheduler.advanceTimeBy(100)
        viewModel.onQueryChange("tapi")
        mainDispatcherRule.dispatcher.scheduler.advanceTimeBy(100)
        viewModel.onQueryChange("tapioka")
        advance(HomeViewModel.SEARCH_DEBOUNCE_MILLIS + 1)

        val searches = sink.events.filter { it.name == "recipe_search" }
        assertEquals(1, searches.size)
        assertEquals("tapioka", searches.single().params["search_term"])
        assertEquals(10, searches.single().params["result_count"])
    }

    @Test
    fun `toggle favorit mencatat event dan status ikut berubah`() = runTest {
        collectState()
        val target = viewModel.state.value.recipes.first { !it.isFavorite }

        viewModel.onFavoriteToggle(target.id)
        advance()
        assertTrue(
            sink.events.any {
                it.name == "recipe_add_favorite" && it.params["recipe_id"] == target.id
            }
        )
        assertTrue(viewModel.state.value.recipes.first { it.id == target.id }.isFavorite)

        viewModel.onFavoriteToggle(target.id)
        advance()
        assertTrue(
            sink.events.any {
                it.name == "recipe_remove_favorite" && it.params["recipe_id"] == target.id
            }
        )
        assertTrue(!viewModel.state.value.recipes.first { it.id == target.id }.isFavorite)
    }

    @Test
    fun `pilih kategori mencatat change_category`() = runTest {
        collectState()

        viewModel.onCategorySelected("Minuman")
        assertTrue(
            sink.events.any {
                it.name == "change_category" && it.params["category"] == "Minuman"
            }
        )

        viewModel.onCategorySelected(null)
        assertTrue(
            sink.events.any {
                it.name == "change_category" && it.params["category"] == "Semua"
            }
        )
    }

    @Test
    fun `pull to refresh memperbarui seed sesi`() = runTest {
        collectState()
        nextSeed = 99L
        viewModel.onRefresh()
        advance()

        assertEquals(99L, catalog.lastSessionSeed)
        assertEquals(10, catalog.lastLimit)
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.state.collect { }
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
