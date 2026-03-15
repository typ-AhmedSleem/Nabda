package com.typ.nabda.caregiver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
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
import com.typ.nabda.core.notifications.NabdaNotificationManager
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

    private val notificationManager: NabdaNotificationManager by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var telemetryClient: TelemetryClient? = null
    private var nsdManager: NsdManager? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
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
    }

    private fun acquireMulticastLock() {
        try {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("nabda-caregiver-lock").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.d("NABDA_CAREGIVER", "MulticastLock acquired")
        } catch (e: Exception) {
            Log.e("NABDA_CAREGIVER", "Failed to acquire MulticastLock", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
                Log.d("NABDA_CAREGIVER", "MulticastLock released")
            }
        } catch (e: Exception) {
            Log.w("NABDA_CAREGIVER", "Error releasing MulticastLock", e)
        }
    }

    private fun registerNetworkCallback() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NABDA_CAREGIVER", "WiFi available - starting discovery")
                startScanning()
            }

            override fun onLost(network: Network) {
                Log.d("NABDA_CAREGIVER", "WiFi lost - stopping discovery")
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
            Log.w("NABDA_CAREGIVER", "Error unregistering network callback", e)
        }
    }

    private fun stopScanning() {
        try {
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (e: Exception) {
            Log.w("NABDA_CAREGIVER", "Failed to stop discovery", e)
        } finally {
            discoveryListener = null
        }
        telemetryClient?.disconnect()
        telemetryClient = null
        LocalClientRegistry.updateStatus(ConnectionStatus.IDLE)
        updateNotification(ConnectionStatus.IDLE)
    }

    private fun startScanning() {
        if (discoveryListener == null) {
            Log.d("NABDA_CAREGIVER", "Discovery listener is null. Initializing...")
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    Log.d("NABDA_CAREGIVER", "Discovery started")
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    Log.d("NABDA_CAREGIVER", "Service found: ${service.serviceName}, Type: ${service.serviceType}")
                    if (service.serviceName.contains(LocalNetworkConstants.SERVICE_NAME)) {
                        Log.d("NABDA_CAREGIVER", "Service name matches - resolving...")
                        nsdManager?.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                Log.e("NABDA_CAREGIVER", "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                            }

                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                Log.d("NABDA_CAREGIVER", "Service resolved: ${serviceInfo.host}:${serviceInfo.port}")
                                val host = serviceInfo.host.hostAddress ?: ""
                                val port = serviceInfo.port
                                pairWithDevice(host, port)
                            }
                        })
                    }
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    Log.d("NABDA_CAREGIVER", "Service lost")
                }

                override fun onDiscoveryStopped(regType: String) {
                    Log.d("NABDA_CAREGIVER", "Discovery stopped")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e("NABDA_CAREGIVER", "Start discovery failed for $serviceType: $errorCode")
                    LocalClientRegistry.updateStatus(ConnectionStatus.FAILED)
                    nsdManager?.stopServiceDiscovery(this)
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.w("NABDA_CAREGIVER", "Stop discovery failed for $serviceType: $errorCode")
                    nsdManager?.stopServiceDiscovery(this)
                }
            }
        } else {
            Log.d("NABDA_CAREGIVER", "Discovery already in progress or listener already initialized.")
        }

        LocalClientRegistry.updateStatus(ConnectionStatus.SCANNING)
        updateNotification(ConnectionStatus.SCANNING)

        Log.d("NABDA_CAREGIVER", "Calling discoverServices")
        nsdManager?.discoverServices(
            LocalNetworkConstants.SERVICE_TYPE,
            NsdManager.PROTOCOL_DNS_SD,
            discoveryListener
        )
    }

    private fun pairWithDevice(host: String, port: Int) {
        LocalClientRegistry.updateStatus(ConnectionStatus.PAIRING)
        updateNotification(ConnectionStatus.PAIRING)

        telemetryClient = TelemetryClient(host, port)
        telemetryClient?.connect()

        LocalClientRegistry.updateStatus(ConnectionStatus.PAIRED)
        updateNotification(ConnectionStatus.PAIRED)

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

            launch {
                telemetryClient?.isConnected?.collect { connected ->
                    if (connected) {
                        LocalClientRegistry.updateStatus(ConnectionStatus.PAIRED)
                        updateNotification(ConnectionStatus.PAIRED)
                    } else {
                        LocalClientRegistry.updateStatus(ConnectionStatus.IDLE)
                        updateNotification(ConnectionStatus.IDLE)
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
            .setContentTitle("Caregiver Connectivity")
            .setContentText("Status: ${status.name}")
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
                "Caregiver Status",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
