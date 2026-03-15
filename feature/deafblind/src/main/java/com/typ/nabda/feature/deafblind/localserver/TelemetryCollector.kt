package com.typ.nabda.feature.deafblind.localserver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
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

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

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
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return null
            val caps = cm.getNetworkCapabilities(network) ?: return null

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                val info = wifiManager.connectionInfo
                android.net.wifi.WifiManager.calculateSignalLevel(info.rssi, 5) // 0-4
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
            val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            am.ringerMode != android.media.AudioManager.RINGER_MODE_NORMAL
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
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return ConnectivitySource.NONE
            val caps = cm.getNetworkCapabilities(network) ?: return ConnectivitySource.NONE
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

    @Suppress("MissingPermission")
    private fun getLocation(): LocationSnapshot? {
        return try {
            val task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            val location = Tasks.await(task) ?: return null
            if (location.accuracy > 50f) {
                Log.d(TAG_SERVER, "Location accuracy ${location.accuracy}m exceeds 50m threshold, skipping")
                return null
            }
            LocationSnapshot(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy,
            )
        } catch (e: SecurityException) {
            Log.w(TAG_SERVER, "Location permission not granted", e)
            null
        } catch (e: Exception) {
            Log.w(TAG_SERVER, "Failed to read location", e)
            null
        }
    }
}
