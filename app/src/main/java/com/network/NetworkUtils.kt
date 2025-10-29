package com.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * İnternet bağlantısı durumunu gözlemleyen ve bildiren yardımcı sınıf.
 */
class NetworkUtils(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Mevcut internet bağlantısı durumunu kontrol eder.
     * @return İnternet bağlantısı varsa true, yoksa false döner.
     */
    fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * İnternet bağlantısı durumunu sürekli olarak gözlemleyen Flow.
     * @return İnternet bağlantısı durumunu bildiren Flow.
     */
    fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Başlangıç değerini gönder
        trySend(isInternetAvailable())

        // Callback'i temizle
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}

/**
 * İnternet bağlantısı durumunu Composable olarak kullanmak için yardımcı fonksiyon.
 * @return İnternet bağlantısı durumunu bildiren State.
 */
@Composable
fun connectivityState(): State<Boolean> {
    val context = LocalContext.current
    val networkUtils = NetworkUtils(context)
    
    return produceState(initialValue = networkUtils.isInternetAvailable()) {
        networkUtils.observeNetworkStatus().collect { isConnected ->
            value = isConnected
        }
    }
} 