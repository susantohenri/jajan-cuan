package com.henrisusanto.vitmate.promo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.util.Locale

/** Cek koneksi internet aktif tanpa memblokir UI. */
fun Context.hasInternetConnection(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

internal fun imageMimeType(url: String): String {
    val extension = url
        .substringBefore('?')
        .substringAfterLast('.', "")
        .lowercase(Locale.US)
    return when (extension) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "heic", "heif" -> "image/heic"
        else -> "image/jpeg"
    }
}

internal fun imageExtension(url: String): String {
    val extension = url
        .substringBefore('?')
        .substringAfterLast('.', "")
        .lowercase(Locale.US)
    return extension.takeIf { it in SUPPORTED_EXTENSIONS } ?: "jpg"
}

private val SUPPORTED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif")
