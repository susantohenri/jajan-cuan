package com.henrisusanto.vitmate.util

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsLogger {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logEvent(name: String, bundle: FirebaseAnalytics.Bundle = FirebaseAnalytics.Bundle()) {
        firebaseAnalytics.logEvent(name, bundle)
    }

    fun logScreenView(screenName: String) {
        logEvent("screen_view", bundle().apply {
            putString("screen_name", screenName)
        })
    }

    fun logViewRecipe(recipeId: String) {
        logEvent("view_recipe", bundle().apply {
            putString("recipe_id", recipeId)
        })
    }

    fun logRecipeSearch(term: String?, resultCount: Int) {
        logEvent("recipe_search", bundle().apply {
            putString("search_term", term ?: "")
            putInt("result_count", resultCount)
        })
    }

    fun logRecipeAddFavorite(recipeId: String) {
        logEvent("recipe_add_favorite", bundle().apply {
            putString("recipe_id", recipeId)
        })
    }

    fun logRecipeRemoveFavorite(recipeId: String) {
        logEvent("recipe_remove_favorite", bundle().apply {
            putString("recipe_id", recipeId)
        })
    }

    fun logViewFavoritesTab(totalCount: Int) {
        logEvent("view_favorites_tab", bundle().apply {
            putInt("total_favorites_count", totalCount)
        })
    }

    fun logCalculateHpp() {
        logEvent("calculate_hpp", bundle())
    }

    fun logSaveCalculation() {
        logEvent("save_calculation", bundle())
    }

    fun logStartCookingTimer() {
        logEvent("start_cooking_timer", bundle())
    }

    fun logRewardUnlock(feature: String, recipeId: String, viaFallback: Boolean) {
        logEvent("rewarded_unlock", bundle().apply {
            putString("feature", feature)
            putString("recipe_id", recipeId)
            putBoolean("via_fallback", viaFallback)
        })
    }

    fun logPromoDownload() {
        logEvent("promo_download", bundle())
    }

    fun logPromoShare() {
        logEvent("promo_share", bundle())
    }

    fun logShareRecipe() {
        logEvent("share_recipe", bundle())
    }

    fun logChangeTheme(themeMode: String) {
        logEvent("change_theme", bundle().apply {
            putString("theme_mode", themeMode)
        })
    }

    fun logAdsEvent(
        adFormat: String,
        adUnitName: String,
        adSource: String,
        screen: String,
        valueMicros: Double? = null,
        currency: String = "USD"
    ) {
        logEvent("jc_ad_paid", bundle().apply {
            putString("ad_format", adFormat)
            putString("ad_unit_name", adUnitName)
            putString("ad_source", adSource)
            putString("screen", screen)
            putDouble("value", valueMicros?.orNull() ?: 0.0)
            putString("currency", currency)
        })
    }

    fun logAdsRequest(adFormat: String, screen: String) {
        logEvent("jc_ad_request", bundle().apply {
            putString("ad_format", adFormat)
            putString("screen", screen)
        })
    }

    fun logAdsClick(adFormat: String, screen: String) {
        logEvent("jc_ad_click", bundle().apply {
            putString("ad_format", adFormat)
            putString("screen", screen)
        })
    }

    fun logAdsFailed(adFormat: String, screen: String, error: String) {
        logEvent("jc_ad_failed", bundle().apply {
            putString("ad_format", adFormat)
            putString("screen", screen)
            putString("error", error)
        })
    }

    fun logUserProperty(key: String, value: String) {
        firebaseAnalytics.setUserProperty(key, value)
    }
}