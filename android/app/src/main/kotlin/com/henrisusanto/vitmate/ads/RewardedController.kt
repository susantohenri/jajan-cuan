package com.henrisusanto.vitmate.ads

import android.app.Activity
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RewardItem(
    val type: String,
    val amount: Int
)

sealed interface RewardedAdState {
    data object Idle : RewardedAdState
    data object Loading : RewardedAdState
    data object Ready : RewardedAdState
    data object TimedOut : RewardedAdState
    data class Failed(val errorCode: Int, val message: String) : RewardedAdState
}

interface RewardedAdGateway {
    fun load(adUnitId: String, callback: Callback)

    interface Callback {
        fun onLoaded(ad: RewardedAdHandle)
        fun onFailed(errorCode: Int, message: String)
    }
}

interface RewardedAdHandle {
    fun show(activity: Activity, callbacks: RewardedAdShowCallbacks)
}

interface RewardedAdShowCallbacks {
    fun onUserEarnedReward(rewardItem: RewardItem)
    fun onPaid(valueMicros: Long, currencyCode: String?, adSource: String?)
    fun onAdClicked()
    fun onAdDismissed()
    fun onShowFailed(errorCode: Int, message: String)
}

/**
 * State machine iklan rewarded. TIDAK membuka fitur dan TIDAK menulis tabel
 * unlocks; itu tugas S9. Controller hanya memuat, retry, timeout, menampilkan,
 * dan meneruskan callback reward.
 *
 * Timeout memakai [clock] (bisa disuntik jam palsu di test) dan retry
 * memakai backoff 3 detik lalu 8 detik (dua retry setelah percobaan pertama).
 */
class RewardedController(
    private val gateway: RewardedAdGateway,
    private val analytics: AnalyticsLogger,
    private val adUnitIdProvider: () -> String,
    private val timeoutMillis: () -> Long,
    private val enabled: () -> Boolean = { true },
    private val screenProvider: () -> String = { "" },
    private val onUserEarnedReward: (RewardItem) -> Unit = {},
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val clock: () -> Long = System::currentTimeMillis,
    private val retryDelaysMillis: List<Long> = DEFAULT_RETRY_DELAYS_MILLIS
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _state = MutableStateFlow<RewardedAdState>(RewardedAdState.Idle)
    val state: StateFlow<RewardedAdState> = _state.asStateFlow()

    private var loadedAd: RewardedAdHandle? = null
    private var loadedAdUnitId: String = ""
    private var timeoutJob: Job? = null
    private var retryJob: Job? = null
    private var retryCount = 0
    private var rewardDelivered = false
    private var destroyed = false
    private var loadInFlight = false

    fun preload() {
        if (destroyed || !enabled() || loadInFlight) return
        when (_state.value) {
            is RewardedAdState.Loading, is RewardedAdState.Ready -> return
            else -> {
                retryCount = 0
                startLoad()
            }
        }
    }

    fun show(activity: Activity) {
        if (destroyed) return
        val ad = loadedAd ?: return
        if (_state.value !is RewardedAdState.Ready) return

        loadedAd = null
        rewardDelivered = false
        val unitIdAtShow = loadedAdUnitId

        ad.show(
            activity,
            object : RewardedAdShowCallbacks {
                override fun onUserEarnedReward(rewardItem: RewardItem) {
                    if (destroyed || rewardDelivered) return
                    rewardDelivered = true
                    onUserEarnedReward(rewardItem)
                }

                override fun onPaid(valueMicros: Long, currencyCode: String?, adSource: String?) {
                    if (destroyed) return
                    analytics.adPaid(
                        format = AdFormat.REWARDED,
                        adUnitId = unitIdAtShow,
                        valueMicros = valueMicros,
                        currencyCode = currencyCode,
                        adSource = adSource,
                        screen = screenProvider()
                    )
                }

                override fun onAdClicked() {
                    if (destroyed) return
                    analytics.adClicked(AdFormat.REWARDED, unitIdAtShow, screenProvider())
                }

                override fun onAdDismissed() {
                    if (destroyed) return
                    _state.value = RewardedAdState.Idle
                }

                override fun onShowFailed(errorCode: Int, message: String) {
                    if (destroyed) return
                    analytics.adFailed(
                        AdFormat.REWARDED,
                        unitIdAtShow,
                        screenProvider(),
                        errorCode,
                        message
                    )
                    _state.value = RewardedAdState.Failed(errorCode, message)
                }
            }
        )
    }

    fun reset() {
        timeoutJob?.cancel()
        retryJob?.cancel()
        retryCount = 0
        rewardDelivered = false
        loadedAd = null
        _state.value = RewardedAdState.Idle
    }

    fun destroy() {
        destroyed = true
        timeoutJob?.cancel()
        retryJob?.cancel()
        loadedAd = null
        scope.cancel()
    }

    private fun startLoad() {
        if (destroyed || loadInFlight) return
        if (!enabled()) {
            _state.value = RewardedAdState.Idle
            return
        }

        loadInFlight = true
        loadedAd = null
        retryJob?.cancel()
        timeoutJob?.cancel()
        _state.value = RewardedAdState.Loading

        val adUnitId = adUnitIdProvider()
        val attemptStartedAt = clock()
        analytics.adRequested(AdFormat.REWARDED, adUnitId, screenProvider())

        timeoutJob = scope.launch {
            val deadline = attemptStartedAt + timeoutMillis()
            while (true) {
                val remaining = deadline - clock()
                if (remaining <= 0) break
                delay(remaining)
            }
            if (!destroyed && _state.value is RewardedAdState.Loading) {
                _state.value = RewardedAdState.TimedOut
                analytics.adFailed(
                    AdFormat.REWARDED,
                    adUnitId,
                    screenProvider(),
                    TIMEOUT_ERROR_CODE,
                    "Rewarded ad tidak siap sebelum timeout"
                )
            }
        }

        gateway.load(
            adUnitId,
            object : RewardedAdGateway.Callback {
                override fun onLoaded(ad: RewardedAdHandle) {
                    loadInFlight = false
                    if (destroyed) return
                    timeoutJob?.cancel()
                    when (_state.value) {
                        is RewardedAdState.Loading, is RewardedAdState.TimedOut -> {
                            loadedAd = ad
                            loadedAdUnitId = adUnitId
                            retryCount = 0
                            _state.value = RewardedAdState.Ready
                        }
                        else -> Unit // hasil basi dari percobaan yang sudah dibatalkan
                    }
                }

                override fun onFailed(errorCode: Int, message: String) {
                    loadInFlight = false
                    if (destroyed) return
                    if (_state.value !is RewardedAdState.Loading) return

                    timeoutJob?.cancel()
                    analytics.adFailed(
                        AdFormat.REWARDED,
                        adUnitId,
                        screenProvider(),
                        errorCode,
                        message
                    )

                    if (retryCount < retryDelaysMillis.size) {
                        val delayMillis = retryDelaysMillis[retryCount]
                        retryCount += 1
                        retryJob = scope.launch {
                            delay(delayMillis)
                            if (!destroyed && _state.value is RewardedAdState.Loading) {
                                startLoad()
                            }
                        }
                    } else {
                        _state.value = RewardedAdState.Failed(errorCode, message)
                    }
                }
            }
        )
    }

    companion object {
        private const val TIMEOUT_ERROR_CODE = 0
        val DEFAULT_RETRY_DELAYS_MILLIS: List<Long> = listOf(3_000L, 8_000L)
    }
}
