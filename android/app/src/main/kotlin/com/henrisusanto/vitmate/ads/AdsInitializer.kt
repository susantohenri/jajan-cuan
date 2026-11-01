package com.henrisusanto.vitmate.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.henrisusanto.vitmate.consent.ConsentManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Inisialisasi MobileAds TEPAT SEKALI dan hanya setelah UMP memberi izin
 * (canRequestAds() == true). Tidak ada permintaan iklan sebelum titik ini.
 */
class AdsInitializer(
    context: Context,
    private val consentManager: ConsentManager,
    private val adManager: AdManager
) {
    private val appContext = context.applicationContext
    private val initializationStarted = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * @return true bila inisialisasi dijalankan (atau sudah pernah dijalankan),
     *         false bila consent belum mengizinkan permintaan iklan.
     */
    fun initializeIfAllowed(): Boolean {
        if (!consentManager.canRequestAds()) {
            Log.d(TAG, "MobileAds tidak diinisialisasi: canRequestAds=false")
            return false
        }
        if (!initializationStarted.compareAndSet(false, true)) {
            return true
        }

        Log.d(TAG, "MobileAds.initialize dimulai setelah consent")
        MobileAds.initialize(appContext) {
            Log.d(TAG, "MobileAds selesai diinisialisasi")
            mainHandler.post { adManager.onMobileAdsInitialized() }
        }
        return true
    }

    fun isInitializationStarted(): Boolean = initializationStarted.get()

    private companion object {
        const val TAG = "AdsInitializer"
    }
}
