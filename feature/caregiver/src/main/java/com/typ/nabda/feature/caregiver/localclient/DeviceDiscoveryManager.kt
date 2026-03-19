package com.typ.nabda.feature.caregiver.localclient

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.SERVICE_NAME
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.SERVICE_TYPE
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.TAG_DISCOVERY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress

/**
 * Discovers the Deaf device on the local network using Android's [NsdManager].
 *
 * Emits the discovered device IP via [discoveredHost]. Automatically re-discovers
 * when WiFi reconnects or the service disappears.
 */
class DeviceDiscoveryManager(private val context: Context) {

    private val _discoveredHost = MutableStateFlow<String?>(null)

    /** Emits the base URL (e.g. "http://192.168.1.42:8080") when a device is found, null when lost. */
    val discoveredHost: StateFlow<String?> = _discoveredHost.asStateFlow()

    private var nsdManager: NsdManager? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var isDiscovering = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // ── Public API ──────────────────────────────────────────────────────────

    fun updateDiscoveredHost(host: String, port: Int) {
        _discoveredHost.value = "http://$host:$port"
    }

    fun clearDiscoveredHost() {
        _discoveredHost.value = null
    }

    fun startDiscovery() {
        acquireMulticastLock()
        registerNetworkCallback()
        /*if (isWifiAvailable()) {
            beginNsdDiscovery()
        } else {
            Log.w(TAG_DISCOVERY, "WiFi not available, waiting for network callback")
        }*/
    }

    fun stopDiscovery() {
        stopNsdDiscovery()
        releaseMulticastLock()
        unregisterNetworkCallback()
        _discoveredHost.value = null
    }

    // ── NSD Discovery ───────────────────────────────────────────────────────

    private fun beginNsdDiscovery() {
        if (isDiscovering) {
            Log.d(TAG_DISCOVERY, "Already discovering, skipping")
            return
        }
        try {
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            isDiscovering = true
            Log.i(TAG_DISCOVERY, "NSD discovery started for $SERVICE_TYPE")
        } catch (e: Exception) {
            Log.e(TAG_DISCOVERY, "Failed to start NSD discovery", e)
        }
    }

    private fun stopNsdDiscovery() {
        if (!isDiscovering) return
        try {
            nsdManager?.stopServiceDiscovery(discoveryListener)
            Log.i(TAG_DISCOVERY, "NSD discovery stopped")
        } catch (e: Exception) {
            Log.w(TAG_DISCOVERY, "Error stopping NSD discovery", e)
        } finally {
            isDiscovering = false
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {

        override fun onDiscoveryStarted(serviceType: String) {
            Log.d(TAG_DISCOVERY, "Discovery started for: $serviceType")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(TAG_DISCOVERY, "Service found: ${serviceInfo.serviceName}")
            if (serviceInfo.serviceName.contains(SERVICE_NAME, ignoreCase = true)) {
                nsdManager?.resolveService(serviceInfo, resolveListener)
            }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.w(TAG_DISCOVERY, "Service lost: ${serviceInfo.serviceName}")
            if (serviceInfo.serviceName.contains(SERVICE_NAME, ignoreCase = true)) {
                _discoveredHost.value = null
            }
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.d(TAG_DISCOVERY, "Discovery stopped for: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG_DISCOVERY, "Start discovery failed: errorCode=$errorCode")
            isDiscovering = false
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.w(TAG_DISCOVERY, "Stop discovery failed: errorCode=$errorCode")
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val host: InetAddress? = serviceInfo.host
            val port: Int = serviceInfo.port
            if (host != null) {
                val baseUrl = "http://${host.hostAddress}:$port"
                Log.i(TAG_DISCOVERY, "Resolved Nabda device at $baseUrl")
                _discoveredHost.value = baseUrl
            } else {
                Log.w(TAG_DISCOVERY, "Resolved service but host is null")
            }
        }

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG_DISCOVERY, "Resolve failed for ${serviceInfo.serviceName}: errorCode=$errorCode")
        }
    }

    // ── MulticastLock ───────────────────────────────────────────────────────

    private fun acquireMulticastLock() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("nabda-mdns").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.i(TAG_DISCOVERY, "MulticastLock acquired")
        } catch (e: Exception) {
            Log.e(TAG_DISCOVERY, "Failed to acquire MulticastLock", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
                Log.i(TAG_DISCOVERY, "MulticastLock released")
            }
        } catch (e: Exception) {
            Log.w(TAG_DISCOVERY, "Error releasing MulticastLock", e)
        }
    }

    // ── Network monitoring ──────────────────────────────────────────────────

    private fun registerNetworkCallback() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.i(TAG_DISCOVERY, "WiFi available — starting discovery")
                beginNsdDiscovery()
            }

            override fun onLost(network: Network) {
                Log.w(TAG_DISCOVERY, "WiFi lost — stopping discovery")
                stopNsdDiscovery()
                _discoveredHost.value = null
            }
        }
        cm.registerNetworkCallback(request, networkCallback!!)
    }

    private fun unregisterNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.w(TAG_DISCOVERY, "Error unregistering network callback", e)
        }
    }

    private fun isWifiAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
