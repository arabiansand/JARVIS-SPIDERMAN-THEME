package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class NetworkStatus {
    AVAILABLE, UNAVAILABLE, LOSING, LOST, WIFI, CELLULAR
}

class NetworkConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val observe: Flow<NetworkStatus> = callbackFlow {
        if (connectivityManager == null) {
            send(NetworkStatus.UNAVAILABLE)
            close()
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch { send(getDetailedNetworkStatus(network)) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                launch { send(NetworkStatus.LOSING) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { send(NetworkStatus.LOST) }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                launch { send(NetworkStatus.UNAVAILABLE) }
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                launch { send(getDetailedNetworkStatus(network)) }
            }
        }

        // Send initial state
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork != null) {
            launch { send(getDetailedNetworkStatus(activeNetwork)) }
        } else {
            launch { send(NetworkStatus.UNAVAILABLE) }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    private fun getDetailedNetworkStatus(network: Network): NetworkStatus {
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        return when {
            capabilities == null -> NetworkStatus.UNAVAILABLE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.AVAILABLE
            else -> NetworkStatus.AVAILABLE
        }
    }
}
