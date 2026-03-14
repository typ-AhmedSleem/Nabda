package com.typ.nabda.feature.deafblind.localserver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.typ.nabda.core.messaging.ActionHandler
import com.typ.nabda.core.model.Action
import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.notifications.NabdaNotificationManager
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.SERVER_HOST
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.SERVER_PORT
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.SERVICE_NAME
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.SERVICE_TYPE
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.TAG_SERVER
import com.typ.nabda.infrastructure.localnetwork.model.ActionAckPayload
import com.typ.nabda.infrastructure.localnetwork.model.GestureAction
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

/**
 * Foreground Service that hosts the Ktor HTTP server and advertises via NsdManager.
 */
class LocalServerService : Service(), KoinComponent {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "nabda_local_server"
        private const val NOTIFICATION_ID = 9001

        fun start(context: Context) {
            val intent = Intent(context, LocalServerService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LocalServerService::class.java))
        }
    }

    private val actionHandler: ActionHandler by inject()
    private val notificationManager: NabdaNotificationManager by inject()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val serverLock = Any()
    private var server: EmbeddedServer<*, *>? = null
    private var isStarting = false

    private var multicastLock: WifiManager.MulticastLock? = null
    private var nsdManager: NsdManager? = null
    private var isRegistered = false

    private lateinit var telemetryCollector: TelemetryCollector

    // ── Lifecycle ───────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        telemetryCollector = TelemetryCollector(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        acquireMulticastLock()
        registerNetworkCallback()
        Log.i(TAG_SERVER, "LocalServerService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isWifiAvailable()) {
//            startServer()
        } else {
            Log.w(TAG_SERVER, "WiFi not available, waiting for network callback")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG_SERVER, "LocalServerService destroyed")
        stopServerInternal()
        releaseMulticastLock()
        unregisterNsd()
        unregisterNetworkCallback()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Server ──────────────────────────────────────────────────────────────

    private fun startServer() {
        synchronized(serverLock) {
            if (server != null || isStarting) {
                Log.d(TAG_SERVER, "Server already running or starting, skipping start")
                return
            }
            isStarting = true
        }

        serviceScope.launch {
            try {
                // Pre-start cleanup to avoid BindException or stale NSD
                stopServerInternalSync()
                unregisterNsd()

                Log.i(TAG_SERVER, "Starting Ktor server on $SERVER_HOST:$SERVER_PORT")
                val deviceId = Build.MODEL ?: "unknown"
                val newServer = embeddedServer(CIO, host = SERVER_HOST, port = SERVER_PORT) {
                    configureDeafServer(
                        telemetryCollector = telemetryCollector,
                        deviceId = deviceId,
                        onActionReceived = { actionPayload ->
                            Log.i(TAG_SERVER, "Local action received: ${actionPayload.action}")

                            val coreAction = mapToCoreAction(actionPayload.action)

                            // Dispatch to internal handler (ViewModel)
                            actionHandler.onActionReceived(coreAction)

                            // Build notification
                            notificationManager.showActionNotification(
                                actionId = coreAction.id,
                                actionName = coreAction.name,
                                priority = coreAction.priority.name
                            )

                            ActionAckPayload(
                                correlationId = actionPayload.correlationId,
                                success = true,
                                message = "Action handled locally",
                            )
                        }
                    )
                }

                newServer.start(wait = false)

                synchronized(serverLock) {
                    server = newServer
                    isStarting = false
                }

                Log.i(TAG_SERVER, "Ktor server started successfully")
                registerNsd()
            } catch (e: Exception) {
                Log.e(TAG_SERVER, "Failed to start server", e)
                synchronized(serverLock) {
                    isStarting = false
                }
            }
        }
    }

    private fun mapToCoreAction(localAction: GestureAction): Action {
        return when (localAction) {
            GestureAction.HELP_REQUEST -> Action(
                "vibrate",
                "Vibration Request",
                "This is a vibration request !",
                ActionPriority.ASSISTANCE
            )

            GestureAction.FALL_ALERT -> Action(
                "alert",
                "Urgent Alert",
                "This is an urgent alert !",
                ActionPriority.EMERGENCY
            )

            else -> Action(
                "unknown",
                "Unknown Action",
                "This is an unknown action !",
                ActionPriority.NORMAL
            )
        }
    }

    private fun stopServerInternal() {
        serviceScope.launch {
            stopServerInternalSync()
        }
    }

    private fun stopServerInternalSync() {
        synchronized(serverLock) {
            val s = server
            server = null
            if (s != null) {
                try {
                    s.stop(1, 5, TimeUnit.SECONDS)
                    Log.i(TAG_SERVER, "Ktor server stopped")
                } catch (e: Exception) {
                    Log.w(TAG_SERVER, "Error stopping server", e)
                }
            }
        }
    }

    // ── NsdManager ──────────────────────────────────────────────────────────

    private fun registerNsd() {
        if (isRegistered) return
        try {
            nsdManager = getSystemService(NSD_SERVICE) as NsdManager
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = SERVICE_NAME
                serviceType = SERVICE_TYPE
                port = SERVER_PORT
            }
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            Log.i(TAG_SERVER, "NSD registration requested: $SERVICE_NAME ($SERVICE_TYPE)")
        } catch (e: Exception) {
            Log.e(TAG_SERVER, "Failed to register NSD service", e)
        }
    }

    private fun unregisterNsd() {
        if (!isRegistered) return
        try {
            nsdManager?.unregisterService(registrationListener)
            Log.i(TAG_SERVER, "NSD service unregistered")
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Error unregistering NSD service", e)
        } finally {
            isRegistered = false
        }
    }

    private val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            isRegistered = true
            Log.i(TAG_SERVER, "NSD service registered: ${serviceInfo.serviceName}")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            isRegistered = false
            Log.e(TAG_SERVER, "NSD registration failed: errorCode=$errorCode")
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            isRegistered = false
            Log.i(TAG_SERVER, "NSD service unregistered: ${serviceInfo.serviceName}")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.w(TAG_SERVER, "NSD unregistration failed: errorCode=$errorCode")
        }
    }

    // ── MulticastLock ───────────────────────────────────────────────────────

    private fun acquireMulticastLock() {
        try {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("nabda-mdns").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.i(TAG_SERVER, "MulticastLock acquired")
        } catch (e: Exception) {
            Log.e(TAG_SERVER, "Failed to acquire MulticastLock", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
                Log.i(TAG_SERVER, "MulticastLock released")
            }
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Error releasing MulticastLock", e)
        }
    }

    // ── Network monitoring ──────────────────────────────────────────────────

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private fun registerNetworkCallback() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.i(TAG_SERVER, "WiFi available — starting server")
                startServer()
            }

            override fun onLost(network: Network) {
                Log.w(TAG_SERVER, "WiFi lost — stopping server")
                unregisterNsd()
                stopServerInternal()
            }
        }
        cm.registerNetworkCallback(request, networkCallback!!)
    }

    private fun unregisterNetworkCallback() {
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Error unregistering network callback", e)
        }
    }

    private fun isWifiAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    // ── Notification ────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Nabda Local Server",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps the local demo server running"
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Nabda Demo Server")
            .setContentText("Local server is running on port $SERVER_PORT")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }
}
