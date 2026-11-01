package com.henrisusanto.vitmate.ads

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.henrisusanto.vitmate.R

/**
 * Mendaftarkan SEMUA asset native ad yang tersedia ke NativeAdView.
 * Asset yang tidak ada disembunyikan, bukan dirender kosong.
 */
internal object NativeAdBinder {
    fun bind(adView: NativeAdView, ad: NativeAd) {
        bindHeadline(adView, ad)
        bindBody(adView, ad)
        bindIcon(adView, ad)
        bindMedia(adView, ad)
        bindCallToAction(adView, ad)
        bindStarRating(adView, ad)
        bindPrice(adView, ad)
        bindStore(adView, ad)
        bindAdvertiser(adView, ad)

        adView.setNativeAd(ad)
    }

    private fun bindHeadline(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<TextView>(R.id.native_ad_headline)
        val text = ad.headline
        if (text.isNullOrBlank()) {
            view.visibility = View.GONE
            adView.headlineView = null
        } else {
            view.text = text
            view.visibility = View.VISIBLE
            adView.headlineView = view
        }
    }

    private fun bindBody(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<TextView>(R.id.native_ad_body)
        val text = ad.body
        if (text.isNullOrBlank()) {
            view.visibility = View.GONE
            adView.bodyView = null
        } else {
            view.text = text
            view.visibility = View.VISIBLE
            adView.bodyView = view
        }
    }

    private fun bindIcon(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<ImageView>(R.id.native_ad_icon)
        val icon = ad.icon
        if (icon == null) {
            view.setImageDrawable(null)
            view.visibility = View.GONE
            adView.iconView = null
        } else {
            view.setImageDrawable(icon.drawable)
            view.visibility = View.VISIBLE
            adView.iconView = view
        }
    }

    private fun bindMedia(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<MediaView>(R.id.native_ad_media)
        val mediaContent = ad.mediaContent
        if (mediaContent == null) {
            view.visibility = View.GONE
            adView.mediaView = null
        } else {
            view.mediaContent = mediaContent
            view.visibility = View.VISIBLE
            adView.mediaView = view
        }
    }

    private fun bindCallToAction(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<TextView>(R.id.native_ad_call_to_action)
        val text = ad.callToAction
        if (text.isNullOrBlank()) {
            view.visibility = View.GONE
            adView.callToActionView = null
        } else {
            view.text = text
            view.visibility = View.VISIBLE
            adView.callToActionView = view
        }
    }

    private fun bindStarRating(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<RatingBar>(R.id.native_ad_star_rating)
        val rating = ad.starRating
        if (rating == null) {
            view.visibility = View.GONE
            adView.starRatingView = null
        } else {
            view.rating = rating.toFloat()
            view.visibility = View.VISIBLE
            adView.starRatingView = view
        }
    }

    private fun bindPrice(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<TextView>(R.id.native_ad_price)
        val text = ad.price
        if (text.isNullOrBlank()) {
            view.visibility = View.GONE
            adView.priceView = null
        } else {
            view.text = text
            view.visibility = View.VISIBLE
            adView.priceView = view
        }
    }

    private fun bindStore(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<TextView>(R.id.native_ad_store)
        val text = ad.store
        if (text.isNullOrBlank()) {
            view.visibility = View.GONE
            adView.storeView = null
        } else {
            view.text = text
            view.visibility = View.VISIBLE
            adView.storeView = view
        }
    }

    private fun bindAdvertiser(adView: NativeAdView, ad: NativeAd) {
        val view = adView.findViewById<TextView>(R.id.native_ad_advertiser)
        val text = ad.advertiser
        if (text.isNullOrBlank()) {
            view.visibility = View.GONE
            adView.advertiserView = null
        } else {
            view.text = text
            view.visibility = View.VISIBLE
            adView.advertiserView = view
        }
    }
}
