package com.henrisusanto.vitmate.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.henrisusanto.vitmate.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Pembungkus Firebase Remote Config.
 *
 * Fetch berjalan non-blocking; UI selalu memakai [values] (nilai default lokal
 * sampai fetch pertama selesai). Interval fetch 12 jam di release, 0 di debug.
 */
class RemoteConfigManager(
    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance(),
    private val isDebugBuild: Boolean,
    private val logTag: String = TAG
) {
    private val started = AtomicBoolean(false)
    private val _values = MutableStateFlow(RemoteConfigValues.DEFAULTS)
    val values: StateFlow<RemoteConfigValues> = _values.asStateFlow()

    fun start() {
        if (!started.compareAndSet(false, true)) return

        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (isDebugBuild) DEBUG_FETCH_INTERVAL_SECONDS else RELEASE_FETCH_INTERVAL_SECONDS)
            .build()

        firebaseRemoteConfig.setConfigSettingsAsync(settings)
            .addOnCompleteListener { configTask ->
                if (!configTask.isSuccessful) {
                    Log.w(logTag, "Gagal menyetel config settings Remote Config", configTask.exception)
                }
                firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
                    .addOnCompleteListener { defaultsTask ->
                        if (!defaultsTask.isSuccessful) {
                            Log.w(logTag, "Gagal memuat default Remote Config", defaultsTask.exception)
                        }
                        publishSnapshot()
                        fetchNonBlocking()
                    }
            }
    }

    fun fetchNonBlocking() {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(logTag, "Remote Config fetchAndActivate sukses")
                } else {
                    Log.w(logTag, "Remote Config fetch gagal; memakai nilai lokal", task.exception)
                }
                publishSnapshot()
            }
    }

    private fun publishSnapshot() {
        val snapshot = readSnapshot()
        _values.value = snapshot
        Log.d(
            logTag,
            "Remote Config aktif: ads=${snapshot.isAdsEnabled}, banner=${snapshot.bannerEnabled}, " +
                "native=${snapshot.nativeEnabled}, rewarded=${snapshot.rewardedEnabled}"
        )
    }

    private fun readSnapshot(): RemoteConfigValues = RemoteConfigValues(
        isAdsEnabled = firebaseRemoteConfig.getBoolean(KEY_IS_ADS_ENABLED),
        bannerEnabled = firebaseRemoteConfig.getBoolean(KEY_BANNER_ENABLED),
        nativeEnabled = firebaseRemoteConfig.getBoolean(KEY_NATIVE_ENABLED),
        rewardedEnabled = firebaseRemoteConfig.getBoolean(KEY_REWARDED_ENABLED),
        bannerAdUnitId = firebaseRemoteConfig.getString(KEY_BANNER_AD_UNIT_ID),
        nativeAdUnitId = firebaseRemoteConfig.getString(KEY_NATIVE_AD_UNIT_ID),
        rewardedAdUnitId = firebaseRemoteConfig.getString(KEY_REWARDED_AD_UNIT_ID),
        nativeEveryN = firebaseRemoteConfig.getLong(KEY_NATIVE_EVERY_N).toInt().coerceAtLeast(1),
        rewardedFallbackUnlockOnFailure = firebaseRemoteConfig.getBoolean(KEY_REWARDED_FALLBACK),
        rewardedReadyTimeoutSec = firebaseRemoteConfig.getLong(KEY_REWARDED_READY_TIMEOUT_SEC).coerceAtLeast(1L),
        metaCheckIntervalHours = firebaseRemoteConfig.getLong(KEY_META_CHECK_INTERVAL_HOURS).coerceAtLeast(1L),
        pageSize = firebaseRemoteConfig.getLong(KEY_PAGE_SIZE).toInt().coerceAtLeast(1)
    )

    private companion object {
        const val TAG = "RemoteConfigManager"
        const val DEBUG_FETCH_INTERVAL_SECONDS = 0L
        const val RELEASE_FETCH_INTERVAL_SECONDS = 12L * 60L * 60L

        const val KEY_IS_ADS_ENABLED = "isAdsEnabled"
        const val KEY_BANNER_ENABLED = "bannerEnabled"
        const val KEY_NATIVE_ENABLED = "nativeEnabled"
        const val KEY_REWARDED_ENABLED = "rewardedEnabled"
        const val KEY_BANNER_AD_UNIT_ID = "bannerAdUnitId"
        const val KEY_NATIVE_AD_UNIT_ID = "nativeAdUnitId"
        const val KEY_REWARDED_AD_UNIT_ID = "rewardedAdUnitId"
        const val KEY_NATIVE_EVERY_N = "nativeEveryN"
        const val KEY_REWARDED_FALLBACK = "rewardedFallbackUnlockOnFailure"
        const val KEY_REWARDED_READY_TIMEOUT_SEC = "rewardedReadyTimeoutSec"
        const val KEY_META_CHECK_INTERVAL_HOURS = "metaCheckIntervalHours"
        const val KEY_PAGE_SIZE = "pageSize"
    }
}
