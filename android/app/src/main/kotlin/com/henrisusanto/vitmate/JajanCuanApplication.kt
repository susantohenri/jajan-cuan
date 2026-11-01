package com.henrisusanto.vitmate

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class JajanCuanApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Cek /meta saat app dibuka dan setiap kembali ke foreground; SyncManager
        // sendiri yang menegakkan throttle metaCheckIntervalHours.
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    container.syncInBackground()
                }
            }
        )
    }
}
