package com.henrisusanto.vitmate.config

import org.junit.Assert.assertEquals
import org.junit.Test

class AdUnitProviderTest {

    private val remoteValues = RemoteConfigValues.DEFAULTS.copy(
        bannerAdUnitId = "prod-banner-unit",
        nativeAdUnitId = "prod-native-unit",
        rewardedAdUnitId = "prod-rewarded-unit"
    )

    @Test
    fun `debug always returns test ids and ignores remote config`() {
        val provider = AdUnitProvider(isDebugBuild = true) { remoteValues }

        assertEquals(AdUnitIds.TEST_BANNER, provider.banner())
        assertEquals(AdUnitIds.TEST_NATIVE, provider.native())
        assertEquals(AdUnitIds.TEST_REWARDED, provider.rewarded())
    }

    @Test
    fun `release uses remote config ids`() {
        val provider = AdUnitProvider(isDebugBuild = false) { remoteValues }

        assertEquals("prod-banner-unit", provider.banner())
        assertEquals("prod-native-unit", provider.native())
        assertEquals("prod-rewarded-unit", provider.rewarded())
    }

    @Test
    fun `release does not fall back to test ids when remote values are blank`() {
        val provider = AdUnitProvider(isDebugBuild = false) {
            remoteValues.copy(
                bannerAdUnitId = "",
                nativeAdUnitId = " ",
                rewardedAdUnitId = ""
            )
        }

        assertEquals("", provider.banner())
        assertEquals("", provider.native())
        assertEquals("", provider.rewarded())
    }
}
