package com.henrisusanto.vitmate.ui.screens

import android.app.Application
import android.net.Uri
import com.henrisusanto.vitmate.ads.RewardedAdState
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.data.image.ImageUrlResolver
import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.data.repository.DetailResult
import com.henrisusanto.vitmate.data.repository.DetailSource
import com.henrisusanto.vitmate.data.repository.FavoriteToggleResult
import com.henrisusanto.vitmate.data.repository.RecipeDetailData
import com.henrisusanto.vitmate.data.repository.SavedCalcState
import com.henrisusanto.vitmate.domain.HppMarginMode
import com.henrisusanto.vitmate.promo.PromoDownloadResult
import com.henrisusanto.vitmate.promo.PromoShareResult
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import com.henrisusanto.vitmate.ui.gate.FeatureGateStatus
import com.henrisusanto.vitmate.ui.testutil.FakeCookingTimers
import com.henrisusanto.vitmate.ui.testutil.FakeDetailCatalog
import com.henrisusanto.vitmate.ui.testutil.FakePromoActions
import com.henrisusanto.vitmate.ui.testutil.FakeRewardedAdSession
import com.henrisusanto.vitmate.ui.testutil.FakeUnlockCatalog
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sink: RecordingAnalyticsSink
    private lateinit var catalog: FakeDetailCatalog
    private lateinit var timers: FakeCookingTimers
    private lateinit var analytics: AnalyticsLogger
    private lateinit var unlocks: FakeUnlockCatalog
    private lateinit var rewardedSession: FakeRewardedAdSession
    private lateinit var promoActions: FakePromoActions
    private val collectedEvents = mutableListOf<DetailUiEvent>()

    @Before
    fun setUp() {
        sink = RecordingAnalyticsSink()
        catalog = FakeDetailCatalog()
        timers = FakeCookingTimers()
        analytics = AnalyticsLogger(
            analytics = sink,
            crashlytics = RecordingCrashlyticsSink(),
            installTimeMillis = 0L,
            nowMillis = { 1L }
        )
        unlocks = FakeUnlockCatalog()
        rewardedSession = FakeRewardedAdSession()
        promoActions = FakePromoActions()
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

    @Test
    fun `timer mencatat event dan tampil di daftar aktif`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())

        val viewModel = createViewModel()
        viewModel.startTimer("Tumis bawang putih", 300)
        advanceUntilIdle()

        val started = timers.started.single()
        assertEquals("Tumis bawang putih", started.label)
        assertEquals(300L, started.durationSeconds)
        assertEquals("Mini Spaghetti Ekonomis", started.recipeTitle)
        assertEquals(
            listOf("Tumis bawang putih"),
            viewModel.state.value.activeTimers.map { it.label }
        )

        val event = sink.events.single { it.name == "start_cooking_timer" }
        assertEquals(
            "mini-spaghetti-ekonomis-untuk-jualan-2ribuan",
            event.params["recipe_id"]
        )
        assertEquals(300L, event.params["duration_sec"])
    }

    @Test
    fun `beberapa timer aktif dan bisa dibatalkan`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())

        val viewModel = createViewModel()
        viewModel.startTimer("Langkah satu", 60)
        viewModel.startTimer("Langkah dua", 120)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.activeTimers.size)
        val firstId = viewModel.state.value.activeTimers.first().id

        viewModel.onCancelTimer(firstId)
        advanceUntilIdle()

        assertEquals(listOf(firstId), timers.cancelled)
        assertEquals(
            listOf("Langkah dua"),
            viewModel.state.value.activeTimers.map { it.label }
        )
    }

    @Test
    fun `durasi timer kurang dari satu detik ditolak`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())

        val viewModel = createViewModel()
        val eventsJob = collectEvents(viewModel)

        viewModel.startTimer("Terlalu cepat", 0)
        advanceUntilIdle()

        assertTrue(timers.started.isEmpty())
        assertTrue(collectedEvents.contains(DetailUiEvent.TimerFailed))
        eventsJob.cancel()
    }

    @Test
    fun `resep tanpa foto promo menyembunyikan konten promosi`() = runTest {
        catalog.detailResult = DetailResult.Success(goldenDetail())

        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.promoImages.isEmpty())
        // Hanya kalkulator yang terkunci, jadi sesi rewarded tetap dipreload.
        assertEquals(1, rewardedSession.preloadCalls)
    }

    @Test
    fun `foto promo memakai origin hosting dan gate siap setelah iklan siap`() = runTest {
        catalog.detailResult = DetailResult.Success(promoDetail())

        val viewModel = createViewModel()

        assertEquals(
            listOf(
                "https://jajan-cuan.web.app/img/promo/1.jpg",
                "https://jajan-cuan.web.app/img/promo/2.jpg"
            ),
            viewModel.state.value.promoImages
        )
        assertEquals(
            FeatureGateStatus.PREPARING,
            viewModel.state.value.calculatorGate.status
        )
        assertEquals(FeatureGateStatus.PREPARING, viewModel.state.value.promoGate.status)

        rewardedSession.setState(RewardedAdState.Ready)
        advanceUntilIdle()

        assertEquals(FeatureGateStatus.READY, viewModel.state.value.calculatorGate.status)
        assertEquals(FeatureGateStatus.READY, viewModel.state.value.promoGate.status)
    }

    @Test
    fun `fitur yang sudah terbuka tidak memicu preload iklan`() = runTest {
        unlocks.unlocked.value = setOf(UnlockFeature.CALCULATOR)
        catalog.detailResult = DetailResult.Success(goldenDetail())

        val viewModel = createViewModel()

        assertEquals(FeatureGateStatus.OPEN, viewModel.state.value.calculatorGate.status)
        assertEquals(0, rewardedSession.preloadCalls)
    }

    @Test
    fun `unduh promo sukses mencatat event dan memberi konfirmasi`() = runTest {
        catalog.detailResult = DetailResult.Success(promoDetail())
        promoActions.downloadResult = PromoDownloadResult.Success

        val viewModel = createViewModel()
        val eventsJob = collectEvents(viewModel)

        viewModel.onPromoDownload(0)
        advanceUntilIdle()

        val (url, baseName) = promoActions.downloadCalls.single()
        assertEquals("https://jajan-cuan.web.app/img/promo/1.jpg", url)
        assertTrue(baseName.startsWith("jajan-cuan-mini-spaghetti-ekonomis"))
        assertTrue(collectedEvents.contains(DetailUiEvent.PromoDownloaded))
        assertEquals(1, countEvent("promo_download"))
        eventsJob.cancel()
    }

    @Test
    fun `unduh promo tanpa internet tidak mencatat event`() = runTest {
        catalog.detailResult = DetailResult.Success(promoDetail())
        promoActions.downloadResult = PromoDownloadResult.NoInternet

        val viewModel = createViewModel()
        val eventsJob = collectEvents(viewModel)

        viewModel.onPromoDownload(1)
        advanceUntilIdle()

        assertTrue(collectedEvents.contains(DetailUiEvent.PromoNoInternet))
        assertEquals(0, countEvent("promo_download"))
        eventsJob.cancel()
    }

    @Test
    fun `share promo sukses mengirim uri dan mencatat event`() = runTest {
        catalog.detailResult = DetailResult.Success(promoDetail())
        val uri = Uri.parse("content://com.henrisusanto.vitmate.fileprovider/promo/foto.jpg")
        promoActions.shareResult = PromoShareResult.Ready(uri, "image/jpeg")

        val viewModel = createViewModel()
        val eventsJob = collectEvents(viewModel)

        viewModel.onPromoShare(0)
        advanceUntilIdle()

        val event = collectedEvents.filterIsInstance<DetailUiEvent.PromoShareReady>().single()
        assertEquals(uri, event.uri)
        assertEquals("image/jpeg", event.mimeType)
        assertEquals(1, countEvent("promo_share"))
        eventsJob.cancel()
    }

    @Test
    fun `share promo gagal memberi pesan kegagalan`() = runTest {
        catalog.detailResult = DetailResult.Success(promoDetail())
        promoActions.shareResult = PromoShareResult.Failed

        val viewModel = createViewModel()
        val eventsJob = collectEvents(viewModel)

        viewModel.onPromoShare(0)
        advanceUntilIdle()

        assertTrue(collectedEvents.contains(DetailUiEvent.PromoShareFailed))
        assertEquals(0, countEvent("promo_share"))
        eventsJob.cancel()
    }

    private fun promoDetail(): RecipeDetailData = goldenDetail().let { detail ->
        detail.copy(
            post = detail.post.copy(
                promoImages = listOf("img/promo/1.jpg", "img/promo/2.jpg")
            )
        )
    }

    private fun TestScope.createViewModel(): DetailViewModel {
        val viewModel = DetailViewModel(
            recipeId = "mini-spaghetti-ekonomis-untuk-jualan-2ribuan",
            repository = catalog,
            timers = timers,
            analytics = analytics,
            unlocks = unlocks,
            rewardedSession = rewardedSession,
            promoActions = promoActions,
            imageUrlResolver = ImageUrlResolver(),
            fallbackUnlockEnabled = { true }
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
