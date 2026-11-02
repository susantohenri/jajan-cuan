package com.henrisusanto.vitmate.ads

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Banner adaptive yang hanya memuat iklan bila consent sudah selesai
 * (MobileAds terinisialisasi) dan flag Remote Config banner/ads aktif.
 *
 * Tinggi slot sebaiknya dicadangkan pemanggil (Home/Favorit) agar layout
 * tidak melompat; komponen ini tidak merender apa pun saat iklan tidak boleh
 * tampil atau lebar kontainer kurang dari 320dp.
 */
@Composable
fun AdaptiveBannerAd(
    adManager: AdManager,
    screen: String,
    modifier: Modifier = Modifier
) {
    val initialized by adManager.initialized.collectAsStateWithLifecycle()
    val config by adManager.config.collectAsStateWithLifecycle()
    val canShow = initialized && config.isAdsEnabled && config.bannerEnabled

    if (!canShow) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()
        if (widthDp >= MIN_ADAPTIVE_BANNER_WIDTH_DP) {
            // [F-032] Ganti key(raw widthDp) dengan bucket kasar (16dp) agar
            // tidak me-recreate AdView saat insets bergeser 1dp.
            // Catatan: AdView melarang setAdSize dipanggil lebih dari sekali,
            // sehingga update{} tidak boleh memanggil setAdSize.
            val widthBucket = widthDp / BANNER_BUCKET_SIZE_DP
            val bucketedWidthDp = widthBucket * BANNER_BUCKET_SIZE_DP
            key(widthBucket) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(
                                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                                    context,
                                    bucketedWidthDp
                                )
                            )
                            adManager.loadBanner(this, screen)
                        }
                    },
                    onRelease = { adView ->
                        adView.destroy()
                    }
                )
            }
        }
    }
}

private const val MIN_ADAPTIVE_BANNER_WIDTH_DP = 320

/** Bucket ukuran lebar untuk menghindari recreation Akibat perubahan 1dp. */
private const val BANNER_BUCKET_SIZE_DP = 16
