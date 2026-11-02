package com.henrisusanto.vitmate.analytics

import com.henrisusanto.vitmate.ads.AdFormat
import com.henrisusanto.vitmate.testutil.RecordingAnalyticsSink
import com.henrisusanto.vitmate.testutil.RecordingCrashlyticsSink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AnalyticsLoggerTest {

    private val analyticsSink = RecordingAnalyticsSink()
    private val crashlyticsSink = RecordingCrashlyticsSink()

    private fun createLogger(
        installTimeMillis: Long = 0L,
        nowMillis: Long = 0L
    ): AnalyticsLogger = AnalyticsLogger(
        analytics = analyticsSink,
        crashlytics = crashlyticsSink,
        installTimeMillis = installTimeMillis,
        nowMillis = { nowMillis }
    )

    @Test
    fun `typed event carries source_platform android`() {
        val logger = createLogger()

        logger.recipeSearch("tapioka", 4)

        val event = analyticsSink.events.last()
        assertEquals("recipe_search", event.name)
        assertEquals("android", event.params["source_platform"])
        assertEquals("tapioka", event.params["search_term"])
        assertEquals(4, event.params["result_count"])
    }

    @Test
    fun `reserved event names are handled gracefully without crash`() {
        val logger = createLogger()
        val reserved = listOf("first_open", "session_start", "ad_click", "ad_impression", "error")

        // Tidak melempar exception, event hanya diabaikan segala adanya.
        reserved.forEach { name ->
            logger.log(name, emptyMap())
        }
    }

    @Test
    fun `ad paid converts micros and falls back to USD`() {
        val logger = createLogger()

        logger.adPaid(
            format = AdFormat.REWARDED,
            adUnitId = "unit-rewarded",
            valueMicros = 12_345L,
            currencyCode = null,
            adSource = null,
            screen = "detail"
        )

        val event = analyticsSink.events.last()
        assertEquals("jc_ad_paid", event.name)
        assertEquals(0.012345, event.params["value"] as Double, 0.0000001)
        assertEquals("USD", event.params["currency"])
        assertEquals("unknown", event.params["ad_source"])
        assertEquals("REWARDED", event.params["ad_format"])
        assertEquals("unit-rewarded", event.params["ad_unit_name"])
        assertEquals("detail", event.params["screen"])
        assertEquals("android", event.params["source_platform"])
    }

    @Test
    fun `ad paid keeps currency and adapter source when present`() {
        val logger = createLogger()

        logger.adPaid(
            format = AdFormat.BANNER,
            adUnitId = "unit-banner",
            valueMicros = 2_500_000L,
            currencyCode = "IDR",
            adSource = "com.google.android.gms.ads.mediation.admob",
            screen = "home"
        )

        val params = analyticsSink.events.last().params
        assertEquals(2.5, params["value"] as Double, 0.0000001)
        assertEquals("IDR", params["currency"])
        assertEquals("com.google.android.gms.ads.mediation.admob", params["ad_source"])
    }

    @Test
    fun `user_type is new before seven days and returning after`() {
        val newUserSink = RecordingAnalyticsSink()
        AnalyticsLogger(
            analytics = newUserSink,
            crashlytics = RecordingCrashlyticsSink(),
            installTimeMillis = 1_000L,
            nowMillis = { 1_000L + 6L * 24L * 60L * 60L * 1000L }
        )
        assertEquals("new", newUserSink.userProperties["user_type"])

        val returningSink = RecordingAnalyticsSink()
        AnalyticsLogger(
            analytics = returningSink,
            crashlytics = RecordingCrashlyticsSink(),
            installTimeMillis = 1_000L,
            nowMillis = { 1_000L + 8L * 24L * 60L * 60L * 1000L }
        )
        assertEquals("returning", returningSink.userProperties["user_type"])
    }

    @Test
    fun `crashlytics custom keys are updated`() {
        val logger = createLogger()

        logger.setThemeMode("dark")
        logger.setAdsEnabled(false)
        logger.setLastScreen("favorites")

        assertEquals("dark", crashlyticsSink.stringKeys["theme_mode"])
        assertEquals("dark", analyticsSink.userProperties["theme_mode"])
        assertFalse(crashlyticsSink.booleanKeys.getValue("ads_enabled"))
        assertEquals("favorites", crashlyticsSink.stringKeys["last_screen"])
    }

    @Test
    fun `change theme logs event and updates property`() {
        val logger = createLogger()

        logger.changeTheme("light")

        assertEquals("change_theme", analyticsSink.events.last().name)
        assertEquals("light", analyticsSink.events.last().params["theme_mode"])
        assertEquals("light", analyticsSink.userProperties["theme_mode"])
    }
}
