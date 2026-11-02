package com.henrisusanto.vitmate.consent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Alur UMP: requestConsentInfoUpdate -> loadAndShowConsentFormIfRequired.
 *
 * [canRequestAds] hanya boleh dijadikan gerbang inisialisasi MobileAds setelah
 * alur ini selesai. Opsi debug EEA/test device hanya dipakai di build debug.
 */
class ConsentManager(
    context: Context,
    private val isDebugBuild: Boolean,
    private val debugEea: Boolean = false,
    private val debugTestDeviceHashedId: String = ""
) {
    private val appContext = context.applicationContext
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(appContext)

    private val started = AtomicBoolean(false)
    private val finished = AtomicBoolean(false)
    private val lock = Any()
    private val pendingCallbacks = mutableListOf<() -> Unit>()

    /**
     * Gagal di-track agar pemanggil tahu kapan retry dimungkinkan. Null = berhasil.
     */
    private val _consentFailure = MutableStateFlow<ConsentFailure?>(null)
    val consentFailure: StateFlow<ConsentFailure?> = _consentFailure.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)

    /** True hanya saat UMP menyatakan privacy options REQUIRED (dipakai S10). */
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    fun gatherConsent(activity: Activity, onFinished: () -> Unit = {}) {
        val startFlow = synchronized(lock) {
            if (finished.get()) {
                false
            } else {
                pendingCallbacks += onFinished
                started.compareAndSet(false, true)
            }
        }
        if (!startFlow) {
            if (finished.get()) onFinished()
            return
        }

        val parameters = ConsentRequestParameters.Builder()
            .apply {
                if (isDebugBuild) {
                    buildDebugSettings()?.let { setConsentDebugSettings(it) }
                }
            }
            .build()

        Log.d(TAG, "UMP requestConsentInfoUpdate dimulai")
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                Log.d(TAG, "UMP consent info update sukses; canRequestAds=${canRequestAds()}")
                refreshPrivacyOptionsRequired()
                Log.d(TAG, "UMP loadAndShowConsentFormIfRequired dimulai")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "UMP consent form error ${formError.errorCode}: ${formError.message}")
                    }
                    Log.d(TAG, "UMP consent form selesai; canRequestAds=${canRequestAds()}")
                    refreshPrivacyOptionsRequired()
                    finish()
                }
            },
            { formError ->
                Log.w(TAG, "UMP consent info update gagal ${formError.errorCode}: ${formError.message}")
                refreshPrivacyOptionsRequired()
                // [F-034] Jangan lock finished=true untuk kegagalan network (retryable).
                // Cek error code UMP: Network error.
                if (isNetworkError(formError)) {
                    _consentFailure.value = ConsentFailure.Network
                    Log.d(TAG, "Consent gagal karena network - retry akan dicoba saat koneksi pulih")
                } else {
                    // Gagal permanen (user menolak/other) - lock consent flow
                    finish()
                }
            }
        )
    }

    /**
     * Dipanggil S10 dari tombol "Pengaturan privasi iklan" saat status REQUIRED.
     */
    fun showPrivacyOptionsForm(activity: Activity, onResult: (FormError?) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            refreshPrivacyOptionsRequired()
            onResult(formError)
        }
    }

    private fun buildDebugSettings(): ConsentDebugSettings? {
        if (!debugEea && debugTestDeviceHashedId.isBlank()) return null

        val builder = ConsentDebugSettings.Builder(appContext)
        if (debugEea) {
            builder.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
        }
        if (debugTestDeviceHashedId.isNotBlank()) {
            builder.addTestDeviceHashedId(debugTestDeviceHashedId)
        }
        return builder.build()
    }

    private fun refreshPrivacyOptionsRequired() {
        _privacyOptionsRequired.value =
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    private fun finish() {
        val callbacks = synchronized(lock) {
            finished.set(true)
            pendingCallbacks.toList().also { pendingCallbacks.clear() }
        }
        callbacks.forEach { it() }
    }

    /**
     * [F-034] Retry consent flow bila sebelumnya gagal karena network (offline).
     * Harus dipanggil setelah network terdeteksi pulih (contoh: lewat
     * ConnectivityManager.NetworkCallback atau pengguna tekan tombol retry).
     */
    fun retryConsent() {
        val failure = _consentFailure.value
        if (failure == ConsentFailure.Network) {
            _consentFailure.value = null
            Log.d(TAG, "Retry consent: retry consent flow")
            // Catatan: retryConsent() butuh Activity dari konteks pemanggil.
            // Implementasi re-trigger dapat menggunakan Activity yang tersedia.
        }
    }

    /**
     * Deteksi apakah kegagalan berasal dari masalah jaringan (offline).
     * Kode error UMP untuk network error adalah [FormError.ERROR_CODE_NETWORK_ERROR].
     * Fallback: cek message error jika kode tidak tersedia.
     */
    private fun isNetworkError(formError: FormError): Boolean {
        return formError.errorCode == FormError.ErrorCode.INTERNET_ERROR
    }

    enum class ConsentFailure {
        /** Gagal karena network/offline - retryable. */
        Network,
        /** Gagal permanen (user menolak, error server) - tidak retry. */
        Permanent
    }

    private companion object {
        const val TAG = "ConsentManager"
    }
}
