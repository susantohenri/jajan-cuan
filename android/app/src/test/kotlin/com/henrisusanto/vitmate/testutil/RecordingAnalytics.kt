package com.henrisusanto.vitmate.testutil

import com.henrisusanto.vitmate.analytics.AnalyticsSink
import com.henrisusanto.vitmate.analytics.CrashlyticsSink

class RecordingAnalyticsSink : AnalyticsSink {
    data class Event(val name: String, val params: Map<String, Any>)

    val events = mutableListOf<Event>()
    val userProperties = mutableMapOf<String, String>()

    override fun logEvent(name: String, params: Map<String, Any>) {
        events += Event(name, params)
    }

    override fun setUserProperty(name: String, value: String) {
        userProperties[name] = value
    }
}

class RecordingCrashlyticsSink : CrashlyticsSink {
    val stringKeys = mutableMapOf<String, String>()
    val booleanKeys = mutableMapOf<String, Boolean>()

    override fun setCustomKey(key: String, value: String) {
        stringKeys[key] = value
    }

    override fun setCustomKey(key: String, value: Boolean) {
        booleanKeys[key] = value
    }
}
