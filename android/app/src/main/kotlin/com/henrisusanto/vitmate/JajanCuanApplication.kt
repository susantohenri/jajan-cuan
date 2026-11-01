package com.henrisusanto.vitmate

import android.app.Application
import com.henrisusanto.vitmate.data.AppContainer
import com.henrisusanto.vitmate.data.AnalyticsLogger

class JajanCuanApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        AnalyticsLogger.init(this)
    }
}