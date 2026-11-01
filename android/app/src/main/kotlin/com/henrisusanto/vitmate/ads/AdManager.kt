package com.henrisusanto.vitmate.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.nativead.NativeAd
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.config.AdUnitProvider
import com.henrisusanto.vitmate.config.RemoteConfigValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pusat iklan Banner, Native, dan Rewarded.
 *
 * Semua pemuatan hanya boleh terjadi setelah [onMobileAdsInitialized] dipanggil
 * oleh AdsInitializer (yang dijaga consent UMP) dan flag Remote Config aktif.
 */
class AdManager(
    context: Context,
    private val analytics: AnalyticsLogger,
    private val adUnitProvider: AdUnitProvider,
    val config: StateFlow<RemoteConfigValues>,
    private val canRequestAds: () -> Boolean
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _initialized = MutableStateFlow(false)
    val initialized: StateFlow<Boolean> = _initialized.asStateFlow()

    private val nativePool = ArrayDeque<NativeAd>()
    private var nativeLoading = 0
    private val _nativePoolSize = MutableStateFlow(0)
    val nativePoolSize: StateFlow<Int> = _nativePoolSize.asStateFlow()

    @Volatile
    var currentScreen: String = SCREEN_HOME
        private set

    fun setCurrentScreen(screen: String) {
        currentScreen = screen
    }

    fun onMobileAdsInitialized() {
        runOnMain {
            _initialized.value = true
            preloadNativeAds()
        }
    }

    fun onConfigChanged(values: RemoteConfigValues) {
        runOnMain {
            if (!values.isAdsEnabled || !values.nativeEnabled) {
                destroyAllNativeAds()
            } else if (isAdvertisingActive()) {
                preloadNativeAds()
            }
        }
    }

    // ---------------------------------------------------------------------
    // Status & konfigurasi
    // ---------------------------------------------------------------------

    fun isAdvertisingActive(): Boolean =
        _initialized.value && canRequestAds() && config.value.isAdsEnabled

    fun canShowBanner(): Boolean = isAdvertisingActive() && config.value.bannerEnabled

    fun canShowNative(): Boolean = isAdvertisingActive() && config.value.nativeEnabled

    fun canShowRewarded(): Boolean = isAdvertisingActive() && config.value.rewardedEnabled

    fun bannerAdUnitId(): String = adUnitProvider.banner()

    fun nativeAdUnitId(): String = adUnitProvider.native()

    fun rewardedAdUnitId(): String = adUnitProvider.rewarded()

    fun nativeEveryN(): Int = config.value.nativeEveryN.coerceAtLeast(1)

    fun rewardedReadyTimeoutMillis(): Long =
        config.value.rewardedReadyTimeoutSec.coerceAtLeast(1L) * 1_000L

    fun rewardedFallbackUnlockOnFailure(): Boolean =
        config.value.rewardedFallbackUnlockOnFailure

    // ---------------------------------------------------------------------
    // Banner
    // ---------------------------------------------------------------------

    /**
     * Memasang listener, mencatat request, lalu memuat banner. Ukuran adaptive
     * sudah ditentukan pemanggil (composable) sebelum method ini dipanggil.
     */
    fun loadBanner(adView: AdView, screen: String = currentScreen) {
        if (!canShowBanner()) return

        val unitId = adUnitProvider.banner()
        adView.adUnitId = unitId
        Log.d(TAG, "Request banner: unit=$unitId screen=$screen")
        analytics.adRequested(AdFormat.BANNER, unitId, screen)
        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                analytics.adClicked(AdFormat.BANNER, unitId, screen)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                analytics.adFailed(AdFormat.BANNER, unitId, screen, error.code, error.message)
            }
        }
        adView.setOnPaidEventListener { adValue ->
            logPaid(AdFormat.BANNER, unitId, adValue, adView.responseInfo, screen)
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    // ---------------------------------------------------------------------
    // Native
    // ---------------------------------------------------------------------

    private fun preloadNativeAds() {
        if (!canShowNative()) return

        val unitId = adUnitProvider.native()
        while (nativePool.size + nativeLoading < NATIVE_POOL_SIZE) {
            nativeLoading += 1

            val adLoader = AdLoader.Builder(appContext, unitId)
                .forNativeAd { nativeAd ->
                    nativeLoading = (nativeLoading - 1).coerceAtLeast(0)
                    if (!isAdvertisingActive() || !config.value.nativeEnabled ||
                        nativePool.size >= NATIVE_POOL_SIZE
                    ) {
                        nativeAd.destroy()
                    } else {
                        nativeAd.setOnPaidEventListener { adValue ->
                            logPaid(
                                AdFormat.NATIVE,
                                unitId,
                                adValue,
                                nativeAd.responseInfo,
                                currentScreen
                            )
                        }
                        nativePool.addLast(nativeAd)
                        _nativePoolSize.value = nativePool.size
                    }
                }
                .withAdListener(
                    object : AdListener() {
                        override fun onAdClicked() {
                            analytics.adClicked(AdFormat.NATIVE, unitId, currentScreen)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            nativeLoading = (nativeLoading - 1).coerceAtLeast(0)
                            analytics.adFailed(
                                AdFormat.NATIVE,
                                unitId,
                                currentScreen,
                                error.code,
                                error.message
                            )
                        }
                    }
                )
                .build()

            Log.d(TAG, "Request native: unit=$unitId screen=$currentScreen")
            analytics.adRequested(AdFormat.NATIVE, unitId, currentScreen)
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    /**
     * Mengambil satu native ad dari pool. Null berarti pool sedang kosong;
     * pemanggil TIDAK boleh merender slot kosong.
     */
    fun takeNativeAd(): NativeAd? {
        if (!canShowNative()) return null

        val ad = nativePool.removeFirstOrNull()
        _nativePoolSize.value = nativePool.size
        if (isAdvertisingActive()) {
            preloadNativeAds()
        }
        return ad
    }

    /**
     * Mengembalikan/menghancurkan native ad yang sudah tidak ditampilkan.
     * Wajib dipanggil saat item dilepas dari Compose atau ViewModel cleared.
     */
    fun releaseNativeAd(nativeAd: NativeAd) {
        runOnMain {
            nativeAd.destroy()
            if (isAdvertisingActive()) {
                preloadNativeAds()
            }
        }
    }

    fun destroyAllNativeAds() {
        val destroyedCount = nativePool.size
        while (nativePool.isNotEmpty()) {
            nativePool.removeFirst().destroy()
        }
        _nativePoolSize.value = 0
        if (destroyedCount > 0) {
            Log.d(TAG, "Semua native ad di pool dihancurkan ($destroyedCount)")
        }
    }

    // ---------------------------------------------------------------------
    // Rewarded
    // ---------------------------------------------------------------------

    fun createRewardedController(
        onUserEarnedReward: (RewardItem) -> Unit = {}
    ): RewardedController = RewardedController(
        gateway = AndroidRewardedAdGateway(appContext),
        analytics = analytics,
        adUnitIdProvider = { adUnitProvider.rewarded() },
        timeoutMillis = { rewardedReadyTimeoutMillis() },
        enabled = { canShowRewarded() },
        screenProvider = { currentScreen },
        onUserEarnedReward = onUserEarnedReward
    )

    // ---------------------------------------------------------------------

    private fun logPaid(
        format: AdFormat,
        adUnitId: String,
        adValue: AdValue,
        responseInfo: ResponseInfo?,
        screen: String
    ) {
        analytics.adPaid(
            format = format,
            adUnitId = adUnitId,
            valueMicros = adValue.valueMicros,
            currencyCode = adValue.currencyCode,
            adSource = AdSourceResolver.from(responseInfo),
            screen = screen
        )
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    companion object {
        const val NATIVE_POOL_SIZE = 3
        const val SCREEN_HOME = "home"
        private const val TAG = "AdManager"
    }
}
