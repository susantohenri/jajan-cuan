package com.henrisusanto.vitmate.config

/**
 * ID unit iklan uji resmi Google. Debug SELALU memakai nilai ini dan tidak
 * pernah membaca Remote Config. Nilai yang sama juga menjadi default di
 * res/xml/remote_config_defaults.xml.
 */
internal object AdUnitIds {
    const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
}

/**
 * Penentu unit iklan. Debug mengabaikan Remote Config; release memakai nilai
 * Remote Config dan kembali ke ID uji hanya bila nilainya kosong.
 */
class AdUnitProvider(
    private val isDebugBuild: Boolean,
    private val valuesProvider: () -> RemoteConfigValues
) {
    fun banner(): String = resolve(isDebugBuild, valuesProvider().bannerAdUnitId, AdUnitIds.TEST_BANNER)

    fun native(): String = resolve(isDebugBuild, valuesProvider().nativeAdUnitId, AdUnitIds.TEST_NATIVE)

    fun rewarded(): String = resolve(isDebugBuild, valuesProvider().rewardedAdUnitId, AdUnitIds.TEST_REWARDED)

    private fun resolve(isDebug: Boolean, remoteValue: String, testValue: String): String =
        if (isDebug) testValue else remoteValue.ifBlank { testValue }
}
