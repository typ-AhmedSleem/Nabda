package com.typ.nabda.infrastructure.fcm.telemetry

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.github.shubham0812.locus.Locus
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.core.model.LocationSnapshot
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class TelemetryCollector(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun collect(deviceId: String): TelemetryHeartbeatPayload = withContext(Dispatchers.IO) {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val batteryPercentage = batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            (level * 100 / scale.toFloat()).toInt()
        } ?: -1

        val isCharging = batteryStatus?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } ?: false

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val connectivitySource = when {
            capabilities == null -> ConnectivitySource.NONE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectivitySource.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectivitySource.CELLULAR
            else -> ConnectivitySource.NONE
        }

        val locationSnapshot = getLocation()

        TelemetryHeartbeatPayload(
            deviceId = deviceId,
            batteryPercentage = batteryPercentage,
            connectivitySource = connectivitySource,
            isCharging = isCharging,
            location = locationSnapshot,
            timestamp = System.currentTimeMillis()
        )
    }

    private suspend fun getLocation(): LocationSnapshot? = suspendCancellableCoroutine { continuation ->
        try {
            Locus.getCurrentLocation(context) { result ->
                val location = result.location
                if (location == null) {
                    if (continuation.isActive) continuation.resume(null)
                    return@getCurrentLocation
                }

                if (location.accuracy > 50f) {
                    if (continuation.isActive) continuation.resume(null)
                } else {
                    if (continuation.isActive) {
                        continuation.resume(
                            LocationSnapshot(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracyMeters = location.accuracy,
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
