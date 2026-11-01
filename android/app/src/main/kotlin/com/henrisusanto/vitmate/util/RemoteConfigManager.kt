package com.henrisusanto.vitmate.util

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteConfigManager(private val context: Context) {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 43200) // 12 hours
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun fetchAndActivate(): Boolean {
        val task = remoteConfig.fetchAndActivate()
        return task.isSuccessful && task.result
    }

    fun isAdsEnabled(): Boolean = remoteConfig.getBoolean("isAdsEnabled")
    fun isBannerEnabled(): Boolean = remoteConfig.getBoolean("bannerEnabled")
    fun isNativeEnabled(): Boolean = remoteConfig.getBoolean("nativeEnabled")
    fun isRewardedEnabled(): Boolean = remoteConfig.getBoolean("rewardedEnabled")
    fun getBannerAdUnitId(): String = remoteConfig.getString("bannerAdUnitId")
    fun getNativeAdUnitId(): String = remoteConfig.getString("nativeAdUnitId")
    fun getRewardedAdUnitId(): String = remoteConfig.getString("rewardedAdUnitId")
    fun getNativeEveryN(): Int = remoteConfig.getLong("nativeEveryN").toInt()
    fun getRewardedFallbackUnlockOnFailure(): Boolean = remoteConfig.getBoolean("rewardedFallbackUnlockOnFailure")
    fun getRewardedReadyTimeoutSec(): Int = remoteConfig.getLong("rewardedReadyTimeoutSec").toInt()
    fun getMetaCheckIntervalHours(): Int = remoteConfig.getLong("metaCheckIntervalHours").toInt()
    fun getPageSize(): Int = remoteConfig.getLong("pageSize").toInt()

    fun fetchInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            fetchAndActivate()
        }
    }
}