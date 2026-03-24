package com.typ.nabda.feature.deafblind.localserver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.util.Log
import com.typ.nabda.core.location.NabdaLocationManager
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.core.model.LocationSnapshot
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.TAG_SERVER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Collects device telemetry data (battery, connectivity, location) from Android system APIs.
 * All collection methods are null-safe and will return null on failure.
 */
class TelemetryCollector(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val locationManager = NabdaLocationManager(context)

    /**
     * Assembles a complete [TelemetryHeartbeatPayload] snapshot.
     */
    suspend fun collect(deviceId: String): TelemetryHeartbeatPayload {
        return withContext(Dispatchers.Default) {
            TelemetryHeartbeatPayload(
                deviceId = deviceId,
                batteryPercentage = getBatteryPercentage(),
                connectivitySource = getConnectivitySource(),
                isCharging = getIsCharging(),
                signalStrength = getSignalStrength(),
                isSilentMode = getIsSilentMode(),
                location = getLocation(),
                timestamp = System.currentTimeMillis(),
            )
        }
    }

    private fun getSignalStrength(): Int? {
        return try {
            val network = connectivityManager.activeNetwork ?: return null
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return null

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                val info = wifiManager.connectionInfo
                WifiManager.calculateSignalLevel(info.rssi, 5) // 0-4
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
                tm.signalStrength?.level ?: 0
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getIsSilentMode(): Boolean? {
        return try {

            audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
        } catch (e: Exception) {
            null
        }
    }

    // ── Battery ─────────────────────────────────────────────────────────────

    private fun getBatteryPercentage(): Int? {
        return try {
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) (level * 100 / scale) else null
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Failed to read battery percentage", e)
            null
        }
    }

    private fun getIsCharging(): Boolean? {
        return try {
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING,
                BatteryManager.BATTERY_STATUS_FULL,
                    -> true

                else -> false
            }
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Failed to read charging state", e)
            null
        }
    }

    // ── Connectivity ────────────────────────────────────────────────────────

    private fun getConnectivitySource(): ConnectivitySource {
        return try {
            val network = connectivityManager.activeNetwork ?: return ConnectivitySource.NONE
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return ConnectivitySource.NONE
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectivitySource.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectivitySource.CELLULAR
                else -> ConnectivitySource.NONE
            }
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Failed to read connectivity source", e)
            ConnectivitySource.NONE
        }
    }

    // ── Location ────────────────────────────────────────────────────────────

    private suspend fun getLocation(): LocationSnapshot? {
        return try {
            locationManager.currentLocation()
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Failed to read your location", e)
            null
        }
    }
}
