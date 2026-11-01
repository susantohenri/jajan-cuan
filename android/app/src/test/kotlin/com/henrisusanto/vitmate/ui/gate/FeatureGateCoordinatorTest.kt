package com.henrisusanto.vitmate.ui.gate

import android.app.Activity
import android.app.Application
import com.henrisusanto.vitmate.ads.RewardedAdState
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import com.henrisusanto.vitmate.ui.testutil.FakeRewardedAdSession
import com.henrisusanto.vitmate.ui.testutil.FakeUnlockCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class FeatureGateCoordinatorTest {

    private lateinit var sink: RecordingAnalyticsSink
    private lateinit var analytics: AnalyticsLogger
    private lateinit var unlocks: FakeUnlockCatalog
    private lateinit var session: FakeRewardedAdSession
    private val activity = Activity()
    private var fallbackEvents = 0

    @Before
    fun setUp() {
        sink = RecordingAnalyticsSink()
        analytics = AnalyticsLogger(
            analytics = sink,
            crashlytics = RecordingCrashlyticsSink(),
            installTimeMillis = 0L,
            nowMillis = { 0L }
        )
        unlocks = FakeUnlockCatalog()
        session = FakeRewardedAdSession()
        fallbackEvents = 0
    }

    @Test
    fun `fitur terkunci disiapkan lalu siap saat iklan siap`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()

        assertEquals(
            FeatureGateStatus.PREPARING,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
        assertEquals(1, session.preloadCalls)

        session.setState(RewardedAdState.Ready)
        advanceUntilIdle()

        assertEquals(
            FeatureGateStatus.READY,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
    }

    @Test
    fun `fitur yang sudah terbuka tidak memicu preload`() = runTest {
        unlocks.unlocked.value = setOf(UnlockFeature.CALCULATOR)
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()

        assertEquals(
            FeatureGateStatus.OPEN,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
        assertEquals(0, session.preloadCalls)
    }

    @Test
    fun `timeout membuka semua fitur terkunci hanya untuk sesi`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(features)
        advanceUntilIdle()

        session.setState(RewardedAdState.TimedOut)
        advanceUntilIdle()

        assertEquals(
            setOf(UnlockFeature.CALCULATOR, UnlockFeature.PROMO),
            unlocks.sessionUnlocks.map { it.second }.toSet()
        )
        assertTrue(unlocks.persisted.isEmpty())
        assertEquals(2, rewardedUnlockEvents().size)
        assertTrue(rewardedUnlockEvents().all { it.params["via_fallback"] == true })
        assertEquals(1, fallbackEvents)
        assertEquals(
            FeatureGateStatus.OPEN,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
        assertEquals(
            FeatureGateStatus.OPEN,
            coordinator.states.value[UnlockFeature.PROMO]?.status
        )
        assertEquals(0, session.showCalls)
    }

    @Test
    fun `gagal setelah retry memakai jalur fallback yang sama`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.PROMO))
        advanceUntilIdle()

        session.setState(RewardedAdState.Failed(103, "no fill"))
        advanceUntilIdle()

        assertEquals(
            listOf(UnlockFeature.PROMO),
            unlocks.sessionUnlocks.map { it.second }
        )
        assertEquals(1, rewardedUnlockEvents().size)
        assertEquals(
            FeatureGateStatus.OPEN,
            coordinator.states.value[UnlockFeature.PROMO]?.status
        )
    }

    @Test
    fun `fallback dimatikan membiarkan fitur tetap terkunci`() = runTest {
        val coordinator = coordinator(fallbackEnabled = false)
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()

        session.setState(RewardedAdState.TimedOut)
        advanceUntilIdle()

        assertTrue(unlocks.sessionUnlocks.isEmpty())
        assertTrue(unlocks.persisted.isEmpty())
        assertTrue(rewardedUnlockEvents().isEmpty())
        assertEquals(0, fallbackEvents)
        assertEquals(
            FeatureGateStatus.PREPARING,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
    }

    @Test
    fun `reward menulis unlocks permanen dan mencatat via fallback false`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()
        session.setState(RewardedAdState.Ready)
        advanceUntilIdle()

        coordinator.onGateClick(UnlockFeature.CALCULATOR)
        advanceUntilIdle()
        assertTrue(
            coordinator.states.value[UnlockFeature.CALCULATOR]?.dialogVisible == true
        )

        coordinator.confirmDialog(activity)
        advanceUntilIdle()
        assertEquals(1, session.showCalls)
        assertFalse(
            coordinator.states.value[UnlockFeature.CALCULATOR]?.dialogVisible == true
        )

        session.earnReward()
        advanceUntilIdle()

        assertEquals(
            listOf(RECIPE_ID to UnlockFeature.CALCULATOR),
            unlocks.persisted
        )
        val event = rewardedUnlockEvents().single()
        assertEquals("CALCULATOR", event.params["feature"])
        assertEquals(false, event.params["via_fallback"])
        assertEquals(RECIPE_ID, event.params["recipe_id"])
        assertEquals(
            FeatureGateStatus.OPEN,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
    }

    @Test
    fun `dialog batal tidak menampilkan iklan`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()
        session.setState(RewardedAdState.Ready)
        advanceUntilIdle()

        coordinator.onGateClick(UnlockFeature.CALCULATOR)
        advanceUntilIdle()
        coordinator.dismissDialog()
        advanceUntilIdle()

        assertFalse(
            coordinator.states.value[UnlockFeature.CALCULATOR]?.dialogVisible == true
        )
        assertEquals(0, session.showCalls)
        assertTrue(unlocks.persisted.isEmpty())

        coordinator.confirmDialog(activity)
        assertEquals(0, session.showCalls)
    }

    @Test
    fun `klik saat belum siap tidak membuka dialog`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()

        coordinator.onGateClick(UnlockFeature.CALCULATOR)
        advanceUntilIdle()

        assertFalse(
            coordinator.states.value[UnlockFeature.CALCULATOR]?.dialogVisible == true
        )
        coordinator.confirmDialog(activity)
        assertEquals(0, session.showCalls)
    }

    @Test
    fun `reward tanpa fitur tertunda tidak menulis apa pun`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()
        session.setState(RewardedAdState.Ready)
        advanceUntilIdle()

        session.earnReward()
        advanceUntilIdle()

        assertTrue(unlocks.persisted.isEmpty())
        assertTrue(rewardedUnlockEvents().isEmpty())
    }

    @Test
    fun `setelah satu fitur terbuka iklan berikutnya dipreload`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(features)
        advanceUntilIdle()
        session.setState(RewardedAdState.Ready)
        advanceUntilIdle()

        coordinator.onGateClick(UnlockFeature.CALCULATOR)
        advanceUntilIdle()
        coordinator.confirmDialog(activity)
        session.earnReward()
        advanceUntilIdle()

        assertEquals(
            listOf(UnlockFeature.CALCULATOR),
            unlocks.persisted.map { it.second }
        )

        // Controller kembali Idle setelah iklan ditutup.
        session.setState(RewardedAdState.Idle)
        advanceUntilIdle()

        assertEquals(2, session.preloadCalls)
        assertEquals(
            FeatureGateStatus.OPEN,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
        assertEquals(
            FeatureGateStatus.PREPARING,
            coordinator.states.value[UnlockFeature.PROMO]?.status
        )
    }

    @Test
    fun `destroy menghentikan pembaruan status`() = runTest {
        val coordinator = coordinator()
        coordinator.updateAvailableFeatures(setOf(UnlockFeature.CALCULATOR))
        advanceUntilIdle()

        coordinator.destroy()
        session.setState(RewardedAdState.Ready)
        advanceUntilIdle()
        session.earnReward()
        advanceUntilIdle()

        assertEquals(
            FeatureGateStatus.PREPARING,
            coordinator.states.value[UnlockFeature.CALCULATOR]?.status
        )
        assertTrue(unlocks.persisted.isEmpty())
    }

    private fun TestScope.coordinator(
        fallbackEnabled: Boolean = true
    ): FeatureGateCoordinator = FeatureGateCoordinator(
        recipeId = RECIPE_ID,
        unlocks = unlocks,
        adSession = session,
        analytics = analytics,
        fallbackEnabled = { fallbackEnabled },
        scope = CoroutineScope(
            backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)
        ),
        onFallback = { fallbackEvents += 1 }
    )

    private fun rewardedUnlockEvents() =
        sink.events.filter { it.name == "rewarded_unlock" }

    private companion object {
        const val RECIPE_ID = "resep-a"
        val features = setOf(UnlockFeature.CALCULATOR, UnlockFeature.PROMO)
    }
}
