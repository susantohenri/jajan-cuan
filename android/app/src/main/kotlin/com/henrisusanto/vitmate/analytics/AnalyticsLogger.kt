package com.henrisusanto.vitmate.analytics

import android.content.Context
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.henrisusanto.vitmate.ads.AdFormat
import java.util.Locale

/**
 * Satu-satunya pintu masuk event Firebase Analytics di aplikasi.
 *
 * Semua event memakai fungsi bertipe (tidak ada nama event liar di layar) dan
 * otomatis membawa parameter [PARAM_SOURCE_PLATFORM] = "android".
 */
class AnalyticsLogger @VisibleForTesting internal constructor(
    private val analytics: AnalyticsSink,
    private val crashlytics: CrashlyticsSink,
    installTimeMillis: Long,
    nowMillis: () -> Long = System::currentTimeMillis,
) {
    init {
        val installedForMillis = (nowMillis() - installTimeMillis).coerceAtLeast(0L)
        val userType = if (installedForMillis < NEW_USER_WINDOW_MILLIS) USER_TYPE_NEW else USER_TYPE_RETURNING
        analytics.setUserProperty(USER_PROPERTY_USER_TYPE, userType)
    }

    fun screenView(screen: String) {
        setLastScreen(screen)
        log(EVENT_SCREEN_VIEW, mapOf(PARAM_SCREEN_NAME to screen))
    }

    fun viewRecipe(recipeId: String) {
        log(EVENT_VIEW_RECIPE, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun changeCategory(category: String) {
        log(EVENT_CHANGE_CATEGORY, mapOf(PARAM_CATEGORY to category))
    }

    fun recipeSearch(searchTerm: String, resultCount: Int) {
        log(
            EVENT_RECIPE_SEARCH,
            mapOf(PARAM_SEARCH_TERM to searchTerm, PARAM_RESULT_COUNT to resultCount)
        )
    }

    fun addFavorite(recipeId: String) {
        log(EVENT_ADD_FAVORITE, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun removeFavorite(recipeId: String) {
        log(EVENT_REMOVE_FAVORITE, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun viewFavoritesTab(totalFavoritesCount: Int) {
        log(EVENT_VIEW_FAVORITES_TAB, mapOf(PARAM_TOTAL_FAVORITES to totalFavoritesCount))
    }

    fun calculateHpp(recipeId: String) {
        log(EVENT_CALCULATE_HPP, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun saveCalculation(recipeId: String) {
        log(EVENT_SAVE_CALCULATION, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun startCookingTimer(recipeId: String, durationSeconds: Long) {
        log(
            EVENT_START_COOKING_TIMER,
            mapOf(PARAM_RECIPE_ID to recipeId, PARAM_DURATION_SEC to durationSeconds)
        )
    }

    fun rewardedUnlock(feature: String, recipeId: String, viaFallback: Boolean) {
        log(
            EVENT_REWARDED_UNLOCK,
            mapOf(
                PARAM_FEATURE to feature,
                PARAM_RECIPE_ID to recipeId,
                PARAM_VIA_FALLBACK to viaFallback
            )
        )
    }

    fun promoDownload(recipeId: String) {
        log(EVENT_PROMO_DOWNLOAD, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun promoShare(recipeId: String) {
        log(EVENT_PROMO_SHARE, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun shareRecipe(recipeId: String) {
        log(EVENT_SHARE_RECIPE, mapOf(PARAM_RECIPE_ID to recipeId))
    }

    fun changeTheme(themeMode: String) {
        setThemeMode(themeMode)
        log(EVENT_CHANGE_THEME, mapOf(PARAM_THEME_MODE to themeMode))
    }

    // ---------------------------------------------------------------------
    // Iklan (event custom jc_*, tidak boleh memakai event reserved Firebase)
    // ---------------------------------------------------------------------

    fun adRequested(format: AdFormat, adUnitId: String, screen: String) {
        log(EVENT_AD_REQUEST, adParams(format, adUnitId, screen))
    }

    fun adClicked(format: AdFormat, adUnitId: String, screen: String) {
        log(EVENT_AD_CLICK, adParams(format, adUnitId, screen))
    }

    fun adFailed(
        format: AdFormat,
        adUnitId: String,
        screen: String,
        errorCode: Int,
        errorMessage: String,
    ) {
        log(
            EVENT_AD_FAILED,
            adParams(format, adUnitId, screen) + mapOf(
                PARAM_ERROR_CODE to errorCode,
                PARAM_ERROR_MESSAGE to errorMessage
            )
        )
    }

    fun adPaid(
        format: AdFormat,
        adUnitId: String,
        valueMicros: Long,
        currencyCode: String?,
        adSource: String?,
        screen: String,
    ) {
        log(
            EVENT_AD_PAID,
            mapOf(
                PARAM_VALUE to valueMicros / MICROS_PER_CURRENCY_UNIT,
                PARAM_CURRENCY to (currencyCode?.takeIf { it.isNotBlank() } ?: CURRENCY_FALLBACK),
                PARAM_AD_FORMAT to format.name,
                PARAM_AD_UNIT_NAME to adUnitId,
                PARAM_AD_SOURCE to (adSource?.takeIf { it.isNotBlank() } ?: AD_SOURCE_UNKNOWN),
                PARAM_SCREEN to screen
            )
        )
    }

    // ---------------------------------------------------------------------
    // User properties & Crashlytics custom keys
    // ---------------------------------------------------------------------

    fun setThemeMode(themeMode: String) {
        analytics.setUserProperty(USER_PROPERTY_THEME_MODE, themeMode)
        crashlytics.setCustomKey(CRASHLYSTICS_THEME_MODE, themeMode)
    }

    fun setAdsEnabled(enabled: Boolean) {
        crashlytics.setCustomKey(CRASHLYSTICS_ADS_ENABLED, enabled)
    }

    fun setLastScreen(screen: String) {
        crashlytics.setCustomKey(CRASHLYSTICS_LAST_SCREEN, screen)
    }

    private fun adParams(format: AdFormat, adUnitId: String, screen: String): Map<String, Any> =
        mapOf(
            PARAM_AD_FORMAT to format.name,
            PARAM_AD_UNIT_NAME to adUnitId,
            PARAM_SCREEN to screen
        )

    @VisibleForTesting
    internal fun log(eventName: String, params: Map<String, Any>) {
        val normalizedName = eventName.trim().lowercase(Locale.US)
        require(normalizedName !in RESERVED_EVENT_NAMES) {
            "Event '$eventName' reserved untuk Firebase dan tidak boleh dicatat manual"
        }
        analytics.logEvent(
            normalizedName,
            params + (PARAM_SOURCE_PLATFORM to SOURCE_PLATFORM)
        )
    }

    companion object {
        const val SOURCE_PLATFORM = "android"

        private const val NEW_USER_WINDOW_MILLIS = 7L * 24L * 60L * 60L * 1000L
        private const val MICROS_PER_CURRENCY_UNIT = 1_000_000.0
        private const val CURRENCY_FALLBACK = "USD"
        private const val AD_SOURCE_UNKNOWN = "unknown"
        private const val USER_TYPE_NEW = "new"
        private const val USER_TYPE_RETURNING = "returning"

        private const val USER_PROPERTY_USER_TYPE = "user_type"
        private const val USER_PROPERTY_THEME_MODE = "theme_mode"

        private const val CRASHLYSTICS_THEME_MODE = "theme_mode"
        private const val CRASHLYSTICS_ADS_ENABLED = "ads_enabled"
        private const val CRASHLYSTICS_LAST_SCREEN = "last_screen"

        private const val EVENT_SCREEN_VIEW = "screen_view"
        private const val EVENT_VIEW_RECIPE = "view_recipe"
        private const val EVENT_CHANGE_CATEGORY = "change_category"
        private const val EVENT_RECIPE_SEARCH = "recipe_search"
        private const val EVENT_ADD_FAVORITE = "recipe_add_favorite"
        private const val EVENT_REMOVE_FAVORITE = "recipe_remove_favorite"
        private const val EVENT_VIEW_FAVORITES_TAB = "view_favorites_tab"
        private const val EVENT_CALCULATE_HPP = "calculate_hpp"
        private const val EVENT_SAVE_CALCULATION = "save_calculation"
        private const val EVENT_START_COOKING_TIMER = "start_cooking_timer"
        private const val EVENT_REWARDED_UNLOCK = "rewarded_unlock"
        private const val EVENT_PROMO_DOWNLOAD = "promo_download"
        private const val EVENT_PROMO_SHARE = "promo_share"
        private const val EVENT_SHARE_RECIPE = "share_recipe"
        private const val EVENT_CHANGE_THEME = "change_theme"

        private const val EVENT_AD_REQUEST = "jc_ad_request"
        private const val EVENT_AD_CLICK = "jc_ad_click"
        private const val EVENT_AD_FAILED = "jc_ad_failed"
        private const val EVENT_AD_PAID = "jc_ad_paid"

        private const val PARAM_SOURCE_PLATFORM = "source_platform"
        private const val PARAM_SCREEN_NAME = "screen_name"
        private const val PARAM_SCREEN = "screen"
        private const val PARAM_RECIPE_ID = "recipe_id"
        private const val PARAM_CATEGORY = "category"
        private const val PARAM_SEARCH_TERM = "search_term"
        private const val PARAM_RESULT_COUNT = "result_count"
        private const val PARAM_TOTAL_FAVORITES = "total_favorites_count"
        private const val PARAM_DURATION_SEC = "duration_sec"
        private const val PARAM_FEATURE = "feature"
        private const val PARAM_VIA_FALLBACK = "via_fallback"
        private const val PARAM_THEME_MODE = "theme_mode"
        private const val PARAM_AD_FORMAT = "ad_format"
        private const val PARAM_AD_UNIT_NAME = "ad_unit_name"
        private const val PARAM_AD_SOURCE = "ad_source"
        private const val PARAM_VALUE = "value"
        private const val PARAM_CURRENCY = "currency"
        private const val PARAM_ERROR_CODE = "error_code"
        private const val PARAM_ERROR_MESSAGE = "error_message"

        private val RESERVED_EVENT_NAMES = setOf(
            "first_open",
            "session_start",
            "ad_click",
            "ad_impression",
            "error"
        )

        @Volatile
        private var instance: AnalyticsLogger? = null

        /**
         * Singleton aplikasi. Dipakai AppContainer; constructor internal dipakai unit test.
         */
        fun getInstance(context: Context): AnalyticsLogger =
            instance ?: synchronized(this) {
                instance ?: create(context.applicationContext).also { instance = it }
            }

        private fun create(context: Context): AnalyticsLogger {
            val installTimeMillis = runCatching {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            }.getOrDefault(System.currentTimeMillis())

            return AnalyticsLogger(
                analytics = FirebaseAnalyticsSink(FirebaseAnalytics.getInstance(context)),
                crashlytics = FirebaseCrashlyticsSink(FirebaseCrashlytics.getInstance()),
                installTimeMillis = installTimeMillis
            )
        }
    }
}

internal interface AnalyticsSink {
    fun logEvent(name: String, params: Map<String, Any>)
    fun setUserProperty(name: String, value: String)
}

internal interface CrashlyticsSink {
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
}

private class FirebaseAnalyticsSink(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsSink {
    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}

private class FirebaseCrashlyticsSink(
    private val firebaseCrashlytics: FirebaseCrashlytics
) : CrashlyticsSink {
    override fun setCustomKey(key: String, value: String) {
        firebaseCrashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        firebaseCrashlytics.setCustomKey(key, value)
    }
}
