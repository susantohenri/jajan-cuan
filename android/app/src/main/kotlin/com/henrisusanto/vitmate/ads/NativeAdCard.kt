package com.henrisusanto.vitmate.ads

import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.henrisusanto.vitmate.R

/**
 * Slot native ad. Tidak merender apa pun selama iklan belum siap (tidak ada
 * slot kosong). Iklan dikembalikan/dihancurkan saat composable keluar dari
 * komposisi; ViewModel yang menyimpan iklan juga wajib memanggil
 * AdManager.releaseNativeAd() saat cleared.
 */
@Composable
fun NativeAdCard(
    adManager: AdManager,
    modifier: Modifier = Modifier
) {
    val initialized by adManager.initialized.collectAsStateWithLifecycle()
    val config by adManager.config.collectAsStateWithLifecycle()
    val poolSize by adManager.nativePoolSize.collectAsStateWithLifecycle()
    val canShow = initialized && config.isAdsEnabled && config.nativeEnabled

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(canShow) {
        if (!canShow) {
            nativeAd = null
        }
    }

    LaunchedEffect(canShow, poolSize) {
        if (canShow && nativeAd == null && poolSize > 0) {
            nativeAd = adManager.takeNativeAd()
        }
    }

    val ad = nativeAd
    DisposableEffect(ad) {
        onDispose {
            if (ad != null) {
                adManager.releaseNativeAd(ad)
            }
        }
    }

    if (canShow && ad != null) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                LayoutInflater.from(context)
                    .inflate(R.layout.item_native_ad, null) as NativeAdView
            },
            update = { view ->
                if (view.tag !== ad) {
                    NativeAdBinder.bind(view, ad)
                    view.tag = ad
                }
            },
            onRelease = { view ->
                view.destroy()
            }
        )
    }
}
