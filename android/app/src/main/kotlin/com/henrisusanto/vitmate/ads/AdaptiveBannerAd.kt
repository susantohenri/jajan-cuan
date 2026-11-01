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
            key(widthDp) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(
                                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                                    context,
                                    widthDp
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
