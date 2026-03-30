package com.typ.nabda.deaf.service

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
import com.typ.nabda.core.haptic.HapticEngine
import com.typ.nabda.deaf.MainActivity
import com.typ.nabda.deaf.helpers.HapticPatternRetriever
import com.typ.nabda.deafblind.R
import com.typ.nabda.feature.deafblind.localserver.TelemetryCollector
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants
import com.typ.nabda.infrastructure.localnetwork.server.LocalKtorServer
import com.typ.nabda.infrastructure.localnetwork.server.LocalServerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground Service that hosts the [LocalKtorServer] for the Deaf-blind app.
 */
class ServerService : Service() {

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount.asStateFlow()

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var ktorServer: LocalKtorServer? = null
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private lateinit var telemetryCollector: TelemetryCollector

    private val hapticEngine: HapticEngine by inject()

    companion object {
        private const val CHANNEL_ID = "nabda_server_channel"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP_SERVER = "com.typ.nabda.ACTION_STOP_SERVER"
        private const val ACTION_START_SERVER = "com.typ.nabda.ACTION_START_SERVER"

        /*// Simple singleton-like access for the UI to observe state
        // In a real app, this would be managed via Koin/Dagger
        var currentInstance: ServerService? = null
            private set

        suspend fun broadcastAction(action: ActionPayload) {
            LocalServerRegistry.activeServer?.broadcastAction(action)
        }

        suspend fun broadcastTelemetry(telemetry: TelemetryHeartbeatPayload) {
            LocalServerRegistry.activeServer?.broadcastTelemetry(telemetry)
        }*/
    }

    override fun onCreate() {
        super.onCreate()
//        currentInstance = this
        nsdManager = getSystemService(NSD_SERVICE) as NsdManager
        telemetryCollector = TelemetryCollector(applicationContext)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(0))

        acquireMulticastLock()
        registerNetworkCallback()
    }

    private fun acquireMulticastLock() {
        try {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("nabda-server-lock").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.d("NABDA_SERVER", "MulticastLock acquired")
        } catch (e: Exception) {
            Log.e("NABDA_SERVER", "Failed to acquire MulticastLock", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
                Log.d("NABDA_SERVER", "MulticastLock released")
            }
        } catch (e: Exception) {
            Log.w("NABDA_SERVER", "Error releasing MulticastLock", e)
        }
    }

    private fun registerNetworkCallback() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NABDA_SERVER", "WiFi available - starting server")
                startServer()
                registerService()
                startTelemetryBroadcasting()
            }

            override fun onLost(network: Network) {
                Log.d("NABDA_SERVER", "WiFi lost - stopping server")
                stopServer()
            }
        }
        cm.registerNetworkCallback(request, networkCallback!!)
    }

    private fun unregisterNetworkCallback() {
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.w("NABDA_SERVER", "Error unregistering network callback", e)
        }
    }

    private fun registerService() {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = LocalNetworkConstants.SERVICE_NAME
            serviceType = LocalNetworkConstants.SERVICE_TYPE
            port = LocalNetworkConstants.SERVER_PORT
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                Log.d("NABDA_SERVER", "Service registered: ${NsdServiceInfo.serviceName}")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NABDA_SERVER", "Registration failed for ${serviceInfo.serviceName}: $errorCode")
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                Log.d("NABDA_SERVER", "Service unregistered successfully")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NABDA_SERVER", "Unregistration failed for ${serviceInfo.serviceName}: $errorCode")
            }
        }

        nsdManager?.registerService(
            serviceInfo,
            NsdManager.PROTOCOL_DNS_SD,
            registrationListener
        )
    }

    private fun startTelemetryBroadcasting() {
        serviceScope.launch {
            while (isActive) {
                if (_connectedClientsCount.value > 0) {
                    val telemetry = telemetryCollector.collect(Build.MODEL ?: "unknown")
                    ktorServer?.broadcastTelemetry(telemetry)
                }
                delay(LocalNetworkConstants.TELEMETRY_DELAY)
            }
        }
    }

    private fun startServer() {
        if (ktorServer != null) return
        val server = LocalKtorServer(
            onClientConnected = {
                val count = ktorServer?.getConnectedClientsCount() ?: 0
                _connectedClientsCount.value = count
                updateNotification(count)
            },
            onClientDisconnected = {
                val count = ktorServer?.getConnectedClientsCount() ?: 0
                _connectedClientsCount.value = count
                updateNotification(count)
            },
            onActionReceived = { actionId ->
                hapticEngine.performHaptic(HapticPatternRetriever.retrievePatternForAction(actionId))
                LocalServerRegistry.emitAckForCaregiverAction(actionId)
            }
        )
        this.ktorServer = server
        LocalServerRegistry.activeServer = server
        server.start()
    }

    private fun stopServer() {
        ktorServer?.stop()
        ktorServer = null
        LocalServerRegistry.activeServer = null
        try {
            registrationListener?.let { nsdManager?.unregisterService(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _connectedClientsCount.value = 0
        updateNotification(0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVER -> {
                Log.d("NABDA_SERVER", "Stop server action triggered from notification")
                stopServer()
            }

            ACTION_START_SERVER -> {
                Log.d("NABDA_SERVER", "Start server action triggered from notification")
                startServer()
                registerService()
                startTelemetryBroadcasting()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopServer()
        releaseMulticastLock()
        unregisterNetworkCallback()
//        currentInstance = null
        super.onDestroy()
    }

    private fun createNotification(clientsCount: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val isRunning = ktorServer != null
        val statusTitle = if (isRunning) getString(R.string.server_status_running) else getString(R.string.server_status_stopped)

        val statusText = if (!isRunning) getString(R.string.server_offline)
        else if (clientsCount == 0) getString(R.string.server_waiting_for_clients)
        else getString(R.string.server_clients_connected, clientsCount)

        val actionText = if (isRunning) getString(R.string.action_stop_server) else getString(R.string.action_start_server)
        val actionIntent = Intent(this, ServerService::class.java).apply {
            action = if (isRunning) ACTION_STOP_SERVER else ACTION_START_SERVER
        }
        val actionPendingIntent = PendingIntent.getService(
            this, 1, actionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(statusTitle)
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(
                NotificationCompat.Action.Builder(
                    if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    actionText,
                    actionPendingIntent
                ).build()
            )

        return builder.build()
    }

    private fun updateNotification(clientsCount: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(clientsCount))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.server_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
