package com.henrisusanto.vitmate.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.henrisusanto.vitmate.ads.AdManager
import com.henrisusanto.vitmate.ads.AdaptiveBannerAd

/**
 * Slot banner sticky di atas bottom nav, hanya di Home/Favorit.
 *
 * Saat iklan boleh tampil, tinggi minimum 56dp dicadangkan agar konten tidak
 * melompat ketika banner selesai dimuat. Bila iklan tidak diizinkan (consent
 * belum ada / flag RC mati), slot tidak mengambil ruang sama sekali.
 */
@Composable
fun StickyBannerSlot(
    adManager: AdManager,
    screen: String,
    modifier: Modifier = Modifier
) {
    val initialized by adManager.initialized.collectAsStateWithLifecycle()
    val config by adManager.config.collectAsStateWithLifecycle()
    val canShow = initialized && config.isAdsEnabled && config.bannerEnabled

    if (!canShow) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            AdaptiveBannerAd(adManager = adManager, screen = screen)
        }
    }
}
