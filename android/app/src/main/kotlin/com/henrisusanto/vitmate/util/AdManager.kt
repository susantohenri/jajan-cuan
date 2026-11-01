package com.henrisusanto.vitmate.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerRewardBasedVideoAd
import com.google.android.gms.ads.appopen.AdManagerAppOpenAd
import com.google.android.gms.ads.formats.nativead.NativeAd
import com.google.android.gms.ads.formats.nativead.NativeAdOptions
import com.google.android.gms.ads.formats.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAdRequest
import com.google.android.ump.UserMessagingPlatform

class AdManager(
    private val context: Context,
    private val adUnitProvider: AdUnitProvider,
    private val consentManager: ConsentManager
) {
    private var bannerAdView: androidx.appcompat.widget.AppCompatAdView? = null
    private var nativeAd: NativeAd? = null
    private var nativeAdView: NativeAdView? = null
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: com.google.android.gms.ads.interstitialad.InterstitialAd? = null
    private var appOpenAd: AdManagerAppOpenAd? = null

    var isConsentGiven: Boolean
        get() = consentManager.canRequestAds()

    init {
        loadAds()
    }

    private fun loadAds() {
        if (!isConsentGiven) {
            Log.d("AdManager", "Consent not given, not loading ads")
            return
        }

        // Load Banner
        loadBanner()

        // Load Native
        loadNative()

        // Load Rewarded
        loadRewarded()

        // Load Interstitial
        loadInterstitial()
    }

    @SuppressLint("InflateParams")
    private fun loadBanner() {
        val adRequest = AdRequest.Builder().build()
        bannerAdView = androidx.appcompat.widget.AppCompatAdView(context).apply {
            adUnitId = adUnitProvider.getBannerAdUnitId()
            loadAd(adRequest)
        }
    }

    @SuppressLint("InflateParams")
    private fun loadNative() {
        AdRequest.Builder().build().also { adRequest ->
            adUnitProvider.getNativeAdUnitId().also { adUnitId ->
                com.google.android.gms.ads.formats.AdManagerAdManagerAdView
                    .loadAd(
                        context,
                        adUnitId,
                        adRequest,
                        ::onNativeAdLoaded
                    )
            }
        }
    }

    private fun onNativeAdLoaded(ad: NativeAd) {
        nativeAd = ad
        // Don't reload the ad until the previous one is destroyed
    }

    @SuppressLint("InflateParams")
    fun showNativeAd(viewGroup: ViewGroup): NativeAdView? {
        if (nativeAd == null) return null

        nativeAd?.let { ad ->
            ad.loadedContentViewState?.takeIf { it.isValid } ?: run {
                // Destroy and reload
                ad.destroy()
                loadNative()
            }
        }

        nativeAd?.let { ad ->
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater
            nativeAdView = inflater.inflate(R.layout.native_ad_view, viewGroup, false) as NativeAdView

            // Register assets
            nativeAdView?.headlineView?.let { headline ->
                headline.text = ad.adSettings?.getString("headline") ?: ""
            }
            nativeAdView?.bodyView?.let { body ->
                body.text = ad.adSettings?.getString("body") ?: ""
            }
            nativeAdView?.callToActionView?.let { callToAction ->
                callToAction.text = ad.adSettings?.getString("call_to_action") ?: ""
            }
            nativeAdView?.iconView?.let { icon ->
                icon.drawable = ad.adSettings?.getDrawable("icon")
            }
            nativeAdView?.store seem

            return nativeAdView
        }
        return null
    }

    private fun loadRewarded() {
        adUnitProvider.getRewardedAdUnitId().also { adUnitId ->
            val adRequest = AdRequest.Builder().build()
            rewardedAd = RewardedAd(context, adUnitId)
            rewardedAd?.loadAd(adRequest, ::onRewardedAdLoaded)
        }
    }

    private fun onRewardedAdLoaded(
        ad: RewardedAd,
        loadCallback: RewardedAdLoadCallback?
    ) {
        rewardedAd = ad
    }

    @SuppressLint("InflateParams")
    fun showRewardedAd(
        activity: android.app.Activity,
        onUserEarnedReward: () -> Unit,
        onFeatureUnlock: (feature: String) -> Unit
    ) {
        if (rewardedAd == null) return

        rewardedAd?.show(
            activity,
            { adValue ->
                // User earned reward
                onUserEarnedReward()
                // Feature unlock - only write to unlocks if not fallback
                // The actual unlocks writing happens in S9
            },
            { error ->
                Log.e("AdManager", "Rewarded ad failed: ${error.message}")
                // Fallback: open feature temporarily without writing to unlocks
                val feature = "calculator" // or determined from context
                onFeatureUnlock(feature)
            }
        )
    }

    private fun loadInterstitial() {
        // Implementation for interstitial
    }
}