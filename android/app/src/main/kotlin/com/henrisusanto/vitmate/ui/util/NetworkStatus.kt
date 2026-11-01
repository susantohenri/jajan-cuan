package com.henrisusanto.vitmate.ui.util

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.henrisusanto.vitmate.promo.hasInternetConnection

/**
 * Memantau koneksi default agar Konten Promosi bisa menampilkan pesan offline
 * yang jelas, bukan grid gambar yang gagal dimuat.
 */
@Composable
fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    var online by remember(context) {
        mutableStateOf(context.hasInternetConnection())
    }

    DisposableEffect(context) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        if (manager == null) {
            online = false
            return@DisposableEffect onDispose { }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                online = context.hasInternetConnection()
            }

            override fun onLost(network: Network) {
                online = context.hasInternetConnection()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                online = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }

        val registered = runCatching {
            manager.registerDefaultNetworkCallback(callback)
        }.isSuccess

        onDispose {
            if (registered) runCatching { manager.unregisterNetworkCallback(callback) }
        }
    }

    return online
}
