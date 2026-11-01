package com.henrisusanto.vitmate.ads

import android.app.Activity
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RewardedControllerTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    private val gateway = FakeRewardedAdGateway()
    private val analytics = AnalyticsLogger(
        analytics = RecordingAnalyticsSink(),
        crashlytics = RecordingCrashlyticsSink(),
        installTimeMillis = 0L,
        nowMillis = { 0L }
    )

    private lateinit var controller: RewardedController

    private fun createController(enabled: Boolean = true): RewardedController =
        RewardedController(
            gateway = gateway,
            analytics = analytics,
            adUnitIdProvider = { TEST_AD_UNIT },
            timeoutMillis = { TIMEOUT_MILLIS },
            enabled = { enabled },
            screenProvider = { "detail" },
            dispatcher = dispatcher,
            clock = { scheduler.currentTime },
            retryDelaysMillis = listOf(3_000L, 8_000L)
        )

    @After
    fun tearDown() {
        if (::controller.isInitialized) {
            controller.destroy()
        }
    }

    @Test
    fun `preload success becomes ready`() {
        controller = createController()

        controller.preload()

        assertEquals(RewardedAdState.Loading, controller.state.value)
        assertEquals(1, gateway.loadCount)

        gateway.succeed()

        assertEquals(RewardedAdState.Ready, controller.state.value)
    }

    @Test
    fun `timeout after rewardedReadyTimeoutSec becomes timed out`() {
        controller = createController()

        controller.preload()
        scheduler.advanceTimeBy(7_999L)
        scheduler.runCurrent()
        assertEquals(RewardedAdState.Loading, controller.state.value)

        scheduler.advanceTimeBy(1L)
        scheduler.runCurrent()

        assertEquals(RewardedAdState.TimedOut, controller.state.value)
        assertEquals(1, gateway.loadCount)
    }

    @Test
    fun `fails after two retries with 3s and 8s backoff`() {
        controller = createController()

        controller.preload()
        gateway.fail(101, "no fill")
        assertEquals(RewardedAdState.Loading, controller.state.value)
        assertEquals(1, gateway.loadCount)

        scheduler.advanceTimeBy(3_000L)
        scheduler.runCurrent()
        assertEquals(2, gateway.loadCount)

        gateway.fail(102, "no fill")
        scheduler.advanceTimeBy(8_000L)
        scheduler.runCurrent()
        assertEquals(3, gateway.loadCount)

        gateway.fail(103, "no fill")
        assertEquals(RewardedAdState.Failed(103, "no fill"), controller.state.value)

        scheduler.advanceTimeBy(60_000L)
        scheduler.runCurrent()
        assertEquals(3, gateway.loadCount)
    }

    @Test
    fun `success on first retry becomes ready and stops retrying`() {
        controller = createController()

        controller.preload()
        gateway.fail(101, "no fill")

        scheduler.advanceTimeBy(3_000L)
        scheduler.runCurrent()
        assertEquals(2, gateway.loadCount)

        gateway.succeed()
        assertEquals(RewardedAdState.Ready, controller.state.value)

        scheduler.advanceTimeBy(60_000L)
        scheduler.runCurrent()
        assertEquals(2, gateway.loadCount)
    }

    @Test
    fun `explicit preload after failure resets retry counter`() {
        controller = createController()

        controller.preload()
        gateway.fail(101, "no fill")
        scheduler.advanceTimeBy(3_000L)
        scheduler.runCurrent()
        gateway.fail(102, "no fill")
        scheduler.advanceTimeBy(8_000L)
        scheduler.runCurrent()
        gateway.fail(103, "no fill")
        assertEquals(RewardedAdState.Failed(103, "no fill"), controller.state.value)

        controller.preload()

        assertEquals(RewardedAdState.Loading, controller.state.value)
        assertEquals(4, gateway.loadCount)
    }

    @Test
    fun `late success after timeout becomes ready`() {
        controller = createController()

        controller.preload()
        scheduler.advanceTimeBy(TIMEOUT_MILLIS)
        scheduler.runCurrent()
        assertEquals(RewardedAdState.TimedOut, controller.state.value)

        gateway.succeed()

        assertEquals(RewardedAdState.Ready, controller.state.value)
    }

    @Test
    fun `preload after timeout does not duplicate in-flight load`() {
        controller = createController()

        controller.preload()
        scheduler.advanceTimeBy(TIMEOUT_MILLIS)
        scheduler.runCurrent()
        assertEquals(RewardedAdState.TimedOut, controller.state.value)

        controller.preload()
        assertEquals(1, gateway.loadCount)

        gateway.succeed()
        assertEquals(RewardedAdState.Ready, controller.state.value)
    }

    @Test
    fun `disabled controller does not request ads`() {
        controller = createController(enabled = false)

        controller.preload()

        assertEquals(RewardedAdState.Idle, controller.state.value)
        assertEquals(0, gateway.loadCount)
    }

    private class FakeRewardedAdGateway : RewardedAdGateway {
        var loadCount = 0
            private set

        private var callback: RewardedAdGateway.Callback? = null

        override fun load(adUnitId: String, callback: RewardedAdGateway.Callback) {
            loadCount += 1
            this.callback = callback
        }

        fun succeed() {
            callback?.onLoaded(FakeRewardedAdHandle)
        }

        fun fail(errorCode: Int, message: String) {
            callback?.onFailed(errorCode, message)
        }
    }

    private object FakeRewardedAdHandle : RewardedAdHandle {
        override fun show(activity: Activity, callbacks: RewardedAdShowCallbacks) = Unit
    }

    private companion object {
        const val TEST_AD_UNIT = "ca-app-pub-3940256099942544/5224354917"
        const val TIMEOUT_MILLIS = 8_000L
    }
}
