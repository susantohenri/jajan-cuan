package com.henrisusanto.vitmate.util

import android.content.Context
import com.henrisusanto.vitmate.BuildConfig

class AdUnitProvider(
    private val remoteConfigManager: RemoteConfigManager,
    private val context: Context
) {
    fun getBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/6300978111"
        } else {
            remoteConfigManager.getBannerAdUnitId()
        }
    }

    fun getNativeAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            remoteConfigManager.getNativeAdUnitId()
        }
    }

    fun getRewardedAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            remoteConfigManager.getRewardedAdUnitId()
        }
    }

    fun getNativeEveryN(): Int {
        return remoteConfigManager.getNativeEveryN()
    }

    fun getRewardedFallbackUnlockOnFailure(): Boolean {
        return remoteConfigManager.getRewardedFallbackUnlockOnFailure()
    }

    fun getRewardedReadyTimeoutSec(): Int {
        return remoteConfigManager.getRewardedReadyTimeoutSec()
    }

    fun isAdsEnabled(): Boolean {
        if (BuildConfig.DEBUG) return true
        return remoteConfigManager.isAdsEnabled()
    }

    fun isBannerEnabled(): Boolean {
        if (BuildConfig.DEBUG) return true
        return remoteConfigManager.isBannerEnabled()
    }

    fun isNativeEnabled(): Boolean {
        if (BuildConfig.DEBUG) return true
        return remoteConfigManager.isNativeEnabled()
    }

    fun isRewardedEnabled(): Boolean {
        if (BuildConfig.DEBUG) return true
        return remoteConfigManager.isRewardedEnabled()
    }
}