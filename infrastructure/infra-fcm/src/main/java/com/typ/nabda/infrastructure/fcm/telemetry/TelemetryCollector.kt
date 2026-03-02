package com.typ.nabda.infrastructure.fcm.telemetry

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.core.model.LocationSnapshot
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationSnapshot = try {
            val locationTask = fusedLocationClient.lastLocation
            val location: Location? = Tasks.await(locationTask)
            if (location != null && location.accuracy < 50f) {
                LocationSnapshot(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = location.accuracy
                )
            } else null
        } catch (e: Exception) {
            null
        }

        TelemetryHeartbeatPayload(
            deviceId = deviceId,
            batteryPercentage = batteryPercentage,
            connectivitySource = connectivitySource,
            isCharging = isCharging,
            location = locationSnapshot,
            timestamp = System.currentTimeMillis()
        )
    }
}
