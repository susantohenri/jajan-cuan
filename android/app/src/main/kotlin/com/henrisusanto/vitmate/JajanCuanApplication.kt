package com.henrisusanto.vitmate

import android.app.Application

class JajanCuanApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
