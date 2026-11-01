package com.henrisusanto.vitmate.ui.screens

import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.data.repository.DetailResult
import com.henrisusanto.vitmate.data.repository.DetailSource
import com.henrisusanto.vitmate.data.repository.FavoriteToggleResult
import com.henrisusanto.vitmate.data.repository.SavedCalcState
import com.henrisusanto.vitmate.domain.HppMarginMode
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import com.henrisusanto.vitmate.ui.testutil.FakeDetailCatalog
import com.henrisusanto.vitmate.ui.testutil.MainDispatcherRule
import com.henrisusanto.vitmate.ui.testutil.goldenDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sink: RecordingAnalyticsSink
    private lateinit var catalog: FakeDetailCatalog
    private lateinit var analytics: AnalyticsLogger
    private val collectedEvents = mutableListOf<DetailUiEvent>()

    @Before
    fun setUp() {
        sink = RecordingAnalyticsSink()
        catalog = FakeDetailCatalog()
        analytics = AnalyticsLogger(
            analytics = sink,
            crashlytics = RecordingCrashlyticsSink(),
            installTimeMillis = 0L,
            nowMillis = { 1L }
        )
    }

    @Test
    fun `favorit offline memuat detail penuh dengan foto lokal`() = runTest {
        catalog.detailResult = DetailResult.Success(
            goldenDetail(source = DetailSource.FAVORITE)
        )
        catalog.favorite.value = true

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals("Mini Spaghetti Ekonomis", state.title)
        assertEquals("/data/user/0/covers/recipe-1/cover", state.coverUrl)
        assertEquals(10, state.ingredients.size)
        assertEquals("25", state.portionsText)
        assertEquals("100", state.marginText)
        assertEquals(HppMarginMode.PERCENT, state.marginMode)
        assertTrue(state.isFavorite)
        assertEquals(39_000.0, state.hpp.totalModal, DELTA)
        assertEquals(1_560.0, state.hpp.hppPerPortion, DELTA)
        assertEquals(3_500L, state.hpp.roundedSellingPrice)
        assertEquals(listOf(300, 900, null), state.steps.map { it.durationSeconds })
        assertEquals(1, catalog.getDetailCalls)
    }

    @Test
    fun `error offline menampilkan pesan dan coba lagi memuat ulang`() = runTest {
        catalog.detailResult = DetailResult.Error(
            "Tidak dapat memuat resep. Periksa koneksi lalu coba lagi."
        )

        val viewModel = createViewModel()
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.errorMessage.orEmpty().contains("memuat"))

        catalog.detailResult = DetailResult.Success(
            goldenDetail(source = DetailSource.CACHE, coverUrl = "https://example.com/a.jpg")
        )
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.state.value.errorMessage)
        assertEquals("Mini Spaghetti Ekonomis", viewModel.state.value.title)
        assertEquals("https://example.com/a.jpg", viewModel.state.value.coverUrl)
        assertEquals(2, catalog.getDetailCalls)
    }

    @Test
    fun `resep tidak ditemukan memakai state notFound`() = runTest {
        catalog.detailResult = DetailResult.NotFound

        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.notFound)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `toggle favorit memperbarui status dan mencatat event`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())
        catalog.toggleResult = FavoriteToggleResult.Added

        val viewModel = createViewModel()
        val eventsJob = collectEvents(viewModel)

        viewModel.onFavoriteToggle()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isFavorite)
        assertTrue(collectedEvents.contains(DetailUiEvent.FavoriteAdded))
        assertTrue(
            sink.events.any {
                it.name == "recipe_add_favorite" &&
                    it.params["recipe_id"] == "mini-spaghetti-ekonomis-untuk-jualan-2ribuan"
            }
        )
        eventsJob.cancel()
    }

    @Test
    fun `calculate hpp dicatat sekali per sesi edit lalu save calculation`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())

        val viewModel = createViewModel()

        viewModel.onPriceChange(0, "0")
        viewModel.onPriceChange(1, "0")
        viewModel.onPortionsChange("25")
        assertEquals(1, countEvent("calculate_hpp"))
        assertEquals(27_000.0, viewModel.state.value.hpp.totalModal, DELTA)

        viewModel.onSaveCalculation()
        advanceUntilIdle()

        assertEquals(1, catalog.saveCalls)
        assertEquals(1, countEvent("save_calculation"))
        assertEquals(27_000.0, catalog.savedState?.prices?.sum()?.toDouble() ?: 0.0, DELTA)
        assertEquals(25, catalog.savedState?.portions)

        // Simpan menutup sesi edit; perubahan berikutnya dicatat lagi.
        viewModel.onMarginChange("50")
        assertEquals(2, countEvent("calculate_hpp"))
    }

    @Test
    fun `simpan kalkulator dimuat ulang saat resep dibuka lagi`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())

        val first = createViewModel()
        first.onPriceChange(0, "5000")
        first.onPortionsChange("10")
        first.onMarginModeChange(HppMarginMode.NOMINAL)
        first.onMarginChange("1000")
        first.onSaveCalculation()
        advanceUntilIdle()

        val second = createViewModel()
        val state = second.state.value

        assertEquals("5000", state.ingredients.first().priceText)
        assertEquals("10", state.portionsText)
        assertEquals(HppMarginMode.NOMINAL, state.marginMode)
        assertEquals("1000", state.marginText)
        assertEquals(35_000.0, state.hpp.totalModal, DELTA)
        assertEquals(3_500.0, state.hpp.hppPerPortion, DELTA)
        assertEquals(4_500.0, state.hpp.sellingPrice, DELTA)
        assertEquals(10_000.0, state.hpp.totalProfit, DELTA)
    }

    @Test
    fun `state tersimpan dipakai sebelum nilai acuan`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())
        catalog.savedState = SavedCalcState(
            prices = listOf(12_000L, 0L),
            portions = 4,
            marginMode = HppMarginMode.PERCENT,
            marginValue = 50.0
        )

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertEquals("12000", state.ingredients[0].priceText)
        assertEquals("0", state.ingredients[1].priceText)
        assertEquals("3000", state.ingredients[2].priceText)
        assertEquals("10000", state.ingredients[9].priceText)
        assertEquals("4", state.portionsText)
        assertEquals("50", state.marginText)
        assertEquals(39_000.0, state.hpp.totalModal, DELTA)
        assertEquals(9_750.0, state.hpp.hppPerPortion, DELTA)
        assertEquals(14_625.0, state.hpp.sellingPrice, DELTA)
    }

    private fun TestScope.createViewModel(): DetailViewModel {
        val viewModel = DetailViewModel(
            recipeId = "mini-spaghetti-ekonomis-untuk-jualan-2ribuan",
            repository = catalog,
            analytics = analytics
        )
        advanceUntilIdle()
        return viewModel
    }

    private fun TestScope.collectEvents(viewModel: DetailViewModel) =
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.events.collect { collectedEvents += it }
        }

    private fun countEvent(name: String): Int = sink.events.count { it.name == name }

    private companion object {
        private const val DELTA = 0.0001
    }
}
