package com.henrisusanto.vitmate.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConsentManager(private val context: Context) {
    private lateinit var consentForm: ConsentForm

    init {
        setupConsent()
    }

    private fun setupConsent() {
        val debugSettings = ConsentDebugSettings.Builder()
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation.requestConsentInfoUpdate(
            context,
            params,
            { },
            { error ->
                Log.e("ConsentManager", "Consent info update failed: ${error.message}")
            }
        )
    }

    fun loadAndShowConsentFormIfRequired(activity: android.app.Activity, onComplete: () -> Unit) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        if (consentInformation.isConsentFormAvailable) {
            UserMessagingPlatform.loadConsentForm(
                context,
                { form ->
                    consentForm = form
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                        consentForm.show(activity) { error ->
                            if (error != null) {
                                Log.e("ConsentManager", "Consent form error: ${error.message}")
                            }
                            onComplete()
                        }
                    } else {
                        onComplete()
                    }
                },
                { error ->
                    Log.e("ConsentManager", "Load consent form failed: ${error.message}")
                    onComplete()
                }
            )
        } else {
            onComplete()
        }
    }

    fun canRequestAds(): Boolean {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        return consentInformation.canRequestAds
    }

    fun showPrivacyOptionsForm(activity: android.app.Activity) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
            loadAndShowConsentFormIfRequired(activity) { }
        }
    }
}