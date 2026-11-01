package com.henrisusanto.vitmate.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Implementasi produksi [RewardedAdGateway] di atas SDK Google Mobile Ads.
 * Semua tipe SDK hanya hidup di file ini agar state machine tetap mudah diuji.
 */
class AndroidRewardedAdGateway(
    context: Context
) : RewardedAdGateway {
    private val appContext = context.applicationContext

    override fun load(adUnitId: String, callback: RewardedAdGateway.Callback) {
        RewardedAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    callback.onLoaded(AndroidRewardedAdHandle(ad))
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    callback.onFailed(adError.code, adError.message)
                }
            }
        )
    }
}

private class AndroidRewardedAdHandle(
    private val ad: RewardedAd
) : RewardedAdHandle {
    override fun show(activity: Activity, callbacks: RewardedAdShowCallbacks) {
        val responseInfo: ResponseInfo? = ad.responseInfo

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                callbacks.onAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                callbacks.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                callbacks.onShowFailed(adError.code, adError.message)
            }
        }

        ad.setOnPaidEventListener { adValue ->
            callbacks.onPaid(
                adValue.valueMicros,
                adValue.currencyCode,
                AdSourceResolver.from(responseInfo)
            )
        }

        ad.show(activity) { rewardItem ->
            callbacks.onUserEarnedReward(RewardItem(rewardItem.type, rewardItem.amount))
        }
    }
}

internal object AdSourceResolver {
    fun from(responseInfo: ResponseInfo?): String? =
        responseInfo?.loadedAdapterResponseInfo?.adapterClassName
            ?: responseInfo?.adapterResponses?.firstOrNull()?.adapterClassName
}
