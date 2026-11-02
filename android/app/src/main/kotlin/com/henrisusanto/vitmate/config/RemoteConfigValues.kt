package com.henrisusanto.vitmate.config

/**
 * Snapshot nilai Remote Config yang dipakai aplikasi.
 *
 * Nilai awal ([DEFAULTS]) sengaja sama dengan res/xml/remote_config_defaults.xml
 * agar UI tidak menunggu fetch jaringan.
 */
data class RemoteConfigValues(
    val isAdsEnabled: Boolean,
    val bannerEnabled: Boolean,
    val nativeEnabled: Boolean,
    val rewardedEnabled: Boolean,
    val bannerAdUnitId: String,
    val nativeAdUnitId: String,
    val rewardedAdUnitId: String,
    val nativeEveryN: Int,
    val rewardedFallbackUnlockOnFailure: Boolean,
    val rewardedReadyTimeoutSec: Long,
    val metaCheckIntervalHours: Long,
    val pageSize: Int
) {
    companion object {
        val DEFAULTS = RemoteConfigValues(
            isAdsEnabled = true,
            bannerEnabled = true,
            nativeEnabled = true,
            rewardedEnabled = true,
            bannerAdUnitId = "",
            nativeAdUnitId = "",
            rewardedAdUnitId = "",
            nativeEveryN = 5,
            rewardedFallbackUnlockOnFailure = true,
            rewardedReadyTimeoutSec = 8,
            metaCheckIntervalHours = 6,
            pageSize = 10
        )
    }
}
