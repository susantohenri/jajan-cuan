package com.henrisusanto.vitmate.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak sesi iklan Rewarded yang dipakai FeatureGate. Implementasi produksi
 * adalah [RewardedController]; test memakai sesi palsu tanpa SDK iklan.
 *
 * Satu sesi hanya memuat satu iklan pada satu waktu. Fitur gate bisa berbagi
 * sesi yang sama, sehingga perilaku "satu iklan untuk satu fitur" tetap utuh.
 */
interface RewardedAdSession {
    val state: StateFlow<RewardedAdState>

    fun preload()

    fun show(activity: Activity)

    fun reset()

    fun destroy()

    /** Dipanggil tepat sekali saat pengguna selesai menonton iklan. */
    fun setRewardListener(listener: (RewardItem) -> Unit)
}
