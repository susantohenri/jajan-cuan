package com.henrisusanto.vitmate

import android.content.Context
import com.henrisusanto.vitmate.ads.AdManager
import com.henrisusanto.vitmate.ads.AdsInitializer
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.config.AdUnitProvider
import com.henrisusanto.vitmate.config.RemoteConfigManager
import com.henrisusanto.vitmate.consent.ConsentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * DI manual aplikasi. Satu instance per proses, dibuat oleh JajanCuanApplication.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val analytics: AnalyticsLogger = AnalyticsLogger.getInstance(appContext)

    val remoteConfigManager = RemoteConfigManager(isDebugBuild = BuildConfig.DEBUG)

    val adUnitProvider = AdUnitProvider(
        isDebugBuild = BuildConfig.DEBUG,
        valuesProvider = { remoteConfigManager.values.value }
    )

    val consentManager = ConsentManager(
        context = appContext,
        isDebugBuild = BuildConfig.DEBUG,
        debugEea = BuildConfig.UMP_DEBUG_EEA,
        debugTestDeviceHashedId = BuildConfig.UMP_TEST_DEVICE_HASHED_ID
    )

    val adManager = AdManager(
        context = appContext,
        analytics = analytics,
        adUnitProvider = adUnitProvider,
        config = remoteConfigManager.values,
        canRequestAds = consentManager::canRequestAds
    )

    val adsInitializer = AdsInitializer(
        context = appContext,
        consentManager = consentManager,
        adManager = adManager
    )

    init {
        remoteConfigManager.start()

        applicationScope.launch {
            remoteConfigManager.values.collect { values ->
                analytics.setAdsEnabled(values.isAdsEnabled)
                adManager.onConfigChanged(values)
            }
        }
    }
}
