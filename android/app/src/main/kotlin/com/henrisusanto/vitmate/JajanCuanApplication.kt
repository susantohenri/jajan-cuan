package com.henrisusanto.vitmate

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache

class JajanCuanApplication : Application(), ImageLoaderFactory {
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

    /**
     * Satu ImageLoader proses-wide dengan disk cache 100 MB (AGENTS §6).
     * Tanpa konfigurasi ini Coil memakai ukuran cache default berbasis ruang
     * kosong, yang tidak sesuai kontrak.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve(IMAGE_CACHE_DIR))
                .maxSizeBytes(IMAGE_CACHE_MAX_BYTES)
                .build()
        }
        .build()

    private companion object {
        const val IMAGE_CACHE_DIR = "image_cache"
        const val IMAGE_CACHE_MAX_BYTES = 100L * 1024L * 1024L
    }
}
