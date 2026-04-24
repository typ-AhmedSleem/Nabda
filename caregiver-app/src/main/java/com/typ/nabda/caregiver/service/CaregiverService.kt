package com.typ.nabda.caregiver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.typ.nabda.caregiver.MainActivity
import com.typ.nabda.caregiver.R
import com.typ.nabda.core.notifications.NabdaNotificationManager
import com.typ.nabda.feature.caregiver.localclient.DeviceDiscoveryManager
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants
import com.typ.nabda.infrastructure.localnetwork.client.ConnectionStatus
import com.typ.nabda.infrastructure.localnetwork.client.LocalClientRegistry
import com.typ.nabda.infrastructure.localnetwork.client.TelemetryClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Foreground Service for the Caregiver app handling mDNS discovery and Ktor client connection.
 */
class CaregiverService : Service(), KoinComponent {

    private val deviceDiscoveryManager by inject<DeviceDiscoveryManager>()
    private val notificationManager by inject<NabdaNotificationManager>()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var telemetryClient: TelemetryClient? = null
    private var nsdManager: NsdManager? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var lastPairedHost: String? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    companion object {
        private const val CHANNEL_ID = "caregiver_service_channel"
        private const val NOTIFICATION_ID = 2001

        var currentInstance: CaregiverService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        currentInstance = this
        nsdManager = getSystemService(NSD_SERVICE) as NsdManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(ConnectionStatus.IDLE))

        acquireMulticastLock()
        registerNetworkCallback()
        startStatusMonitoring()
    }

    private fun startStatusMonitoring() {
        serviceScope.launch {
            var lastStatus: ConnectionStatus? = null
            LocalClientRegistry.status.collect { status ->
                updateNotification(status)

                // Dismiss notification if connected
                if (status == ConnectionStatus.CONNECTED) {
                    notificationManager.dismissDisconnectionNotification()
                }

                // Push disconnection notification only once when connection is lost
                if (lastStatus == ConnectionStatus.CONNECTED && (status == ConnectionStatus.DISCONNECTED || status == ConnectionStatus.FAILED)) {
                    notificationManager.showDisconnectionNotification(
                        title = getString(com.typ.nabda.feature.caregiver.R.string.nabda_caregiver),
                        message = getString(com.typ.nabda.feature.caregiver.R.string.connection_lost)
                    )
                }
                lastStatus = status

                // Handle registry updates based on status
                if (status == ConnectionStatus.IDLE || status == ConnectionStatus.FAILED) {
                    LocalClientRegistry.updateConnectedHostUrl(null)
                    deviceDiscoveryManager.clearDiscoveredHost()
                }
            }
        }
    }

    private fun acquireMulticastLock() {
        try {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("nabda-caregiver-lock").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.d("NABDA_CaregiverService", "MulticastLock acquired")
        } catch (e: Exception) {
            Log.e("NABDA_CaregiverService", "Failed to acquire MulticastLock", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
                Log.d("NABDA_CaregiverService", "MulticastLock released")
            }
        } catch (e: Exception) {
            Log.w("NABDA_CaregiverService", "Error releasing MulticastLock", e)
        }
    }

    private fun registerNetworkCallback() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NABDA_CaregiverService", "WiFi available - starting discovery")
                startScanning()
            }

            override fun onLost(network: Network) {
                Log.d("NABDA_CaregiverService", "WiFi lost - stopping discovery")
                stopScanning()
            }
        }
        cm.registerNetworkCallback(request, networkCallback!!)
    }

    private fun unregisterNetworkCallback() {
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.w("NABDA_CaregiverService", "Error unregistering network callback", e)
        }
    }

    private fun stopScanning() {
        try {
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (e: Exception) {
            Log.w("NABDA_CaregiverService", "Failed to stop discovery", e)
        } finally {
            discoveryListener = null
        }
        telemetryClient?.stop()
        telemetryClient = null
        LocalClientRegistry.updateStatus(ConnectionStatus.IDLE)
        updateNotification(ConnectionStatus.IDLE)
    }

    private fun startScanning() {
        if (discoveryListener == null) {
            Log.d("NABDA_CaregiverService", "Discovery listener is null. Initializing...")
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    Log.d("NABDA_CaregiverService", "Discovery started")
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    Log.d("NABDA_CaregiverService", "Service found: ${service.serviceName}, Type: ${service.serviceType}")
                    if (service.serviceName.contains(LocalNetworkConstants.SERVICE_NAME)) {
                        Log.d("NABDA_CaregiverService", "Service name matches - resolving...")
                        nsdManager?.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                Log.e("NABDA_CaregiverService", "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                            }

                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                Log.d("NABDA_CaregiverService", "Service resolved: ${serviceInfo.host}:${serviceInfo.port}")
                                val host = serviceInfo.host.hostAddress ?: ""
                                val port = serviceInfo.port

                                val currentStatus = LocalClientRegistry.status.value
                                val isAlreadyConnecting = currentStatus == ConnectionStatus.CONNECTING ||
                                        currentStatus == ConnectionStatus.CONNECTED ||
                                        currentStatus == ConnectionStatus.RECONNECTING

                                // Only pair if we are not already connected/connecting OR if the host changed
                                if (!isAlreadyConnecting || host != lastPairedHost) {
                                    pairWithDevice(host, port)
                                } else {
                                    Log.d("NABDA_CaregiverService", "Skipping pairing, already connected/connecting to $host or host is the same.")
                                }
                            }
                        })
                    }
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    Log.d("NABDA_CaregiverService", "Service lost")
                    deviceDiscoveryManager.clearDiscoveredHost()
                }

                override fun onDiscoveryStopped(regType: String) {
                    Log.d("NABDA_CaregiverService", "Discovery stopped")
                    deviceDiscoveryManager.clearDiscoveredHost()
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e("NABDA_CaregiverService", "Start discovery failed for $serviceType: $errorCode")
                    LocalClientRegistry.updateStatus(ConnectionStatus.FAILED)
                    nsdManager?.stopServiceDiscovery(this)
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.w("NABDA_CaregiverService", "Stop discovery failed for $serviceType: $errorCode")
                    nsdManager?.stopServiceDiscovery(this)
                }
            }
        } else {
            Log.d("NABDA_CaregiverService", "Discovery already in progress or listener already initialized.")
        }

        LocalClientRegistry.updateStatus(ConnectionStatus.SCANNING)
        updateNotification(ConnectionStatus.SCANNING)

        Log.d("NABDA_CaregiverService", "Calling discoverServices")
        nsdManager?.discoverServices(
            LocalNetworkConstants.SERVICE_TYPE,
            NsdManager.PROTOCOL_DNS_SD,
            discoveryListener
        )
    }

    fun pairWithDevice(host: String, port: Int) {
        if (lastPairedHost == host && LocalClientRegistry.status.value == ConnectionStatus.CONNECTED) {
            Log.d("NABDA_CaregiverService", "Already connected to $host, skipping.")
            return
        }

        Log.i("NABDA_CaregiverService", "Pairing with device at $host:$port (Previous: $lastPairedHost)")
        lastPairedHost = host

        // Stop current client if any
        telemetryClient?.stop()
        telemetryClient = null

        // Update registry and UI
        LocalClientRegistry.updateStatus(ConnectionStatus.PAIRING)
        updateNotification(ConnectionStatus.PAIRING)

        telemetryClient = TelemetryClient(host, port)
        telemetryClient?.connect()

        LocalClientRegistry.updateConnectedHostUrl("http://$host:$port")
        deviceDiscoveryManager.updateDiscoveredHost(host, port)

        serviceScope.launch {
            launch {
                telemetryClient?.telemetryEvents?.collect {
                    LocalClientRegistry.updateTelemetry(it)
                }
            }
            launch {
                telemetryClient?.actionEvents?.collect {
                    LocalClientRegistry.updateAlert(it)
                    notificationManager.showActionNotification(
                        actionId = it.correlationId,
                        actionName = it.title,
                        priority = it.priority.name
                    )
                }
            }
            // Observation of status is now handled reactive by startStatusMonitoring()
            launch {
                LocalClientRegistry.status.collect { status ->
                    if (status == ConnectionStatus.CONNECTED) {
                        LocalClientRegistry.updateConnectedHostUrl("http://$host:$port")
                        deviceDiscoveryManager.updateDiscoveredHost(host, port)
                    }
                }
            }
        }
    }

    fun acknowledgeAlert(actionId: String) {
        serviceScope.launch {
            telemetryClient?.acknowledgeAlert(actionId)
            LocalClientRegistry.updateAlert(null)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopScanning()
        telemetryClient?.stop()
        telemetryClient = null
        lastPairedHost = null
        releaseMulticastLock()
        unregisterNetworkCallback()
        currentInstance = null
        super.onDestroy()
    }

    private fun createNotification(status: ConnectionStatus): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.caregiver_connectivity))
            .setContentText(getString(R.string.connection_status_prefix, getString(status.resId)))
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(status: ConnectionStatus) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(status))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.caregiver_status_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
