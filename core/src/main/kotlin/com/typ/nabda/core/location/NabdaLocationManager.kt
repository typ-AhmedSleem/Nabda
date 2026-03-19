package com.typ.nabda.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.location.Geocoder
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.typ.nabda.core.model.LocationSnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import android.location.Location as AndroidLocation

class NabdaLocationManager(private val context: Context) {

    private val geocoder = Geocoder(context, Locale.getDefault())
    private val locationSettings = LocationServices.getSettingsClient(context)
    private val locationManager = LocationServices.getFusedLocationProviderClient(context)

    private var lastKnownLocation: AndroidLocation? = null
    private var lastKnownLocationRetrieveTime = 0L

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): LocationSnapshot? {
        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            continuation.invokeOnCancellation {
                cts.cancel()
            }

            try {
                val locationSettingsRequest = LocationSettingsRequest
                    .Builder()
                    .setAlwaysShow(false)
                    .build()
                locationSettings
                    .checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener {
                        val currentLocationRequest = CurrentLocationRequest.Builder()
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .build()

                        locationManager
                            .getCurrentLocation(currentLocationRequest, cts.token)
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    Log.d("Nabda_LocationManager", "Retrieved current location from system.")
                                    lastKnownLocation = location
                                    lastKnownLocationRetrieveTime = System.currentTimeMillis()
                                    continuation.resume(
                                        LocationSnapshot(
                                            latitude = location.latitude,
                                            longitude = location.longitude,
                                            accuracyMeters = location.accuracy
                                        )
                                    )
                                } else {
                                    Log.w("Nabda_LocationManager", "Current location is null.")
                                    continuation.resume(null)
                                }
                            }
                            .addOnFailureListener {
                                Log.w("Nabda_LocationManager", "Failed to retrieve current location. Reason='${it.message}'.")
                                it.printStackTrace()
                                continuation.resume(null)
                            }
                    }
                    .addOnFailureListener {
                        Log.w("Nabda_LocationManager", "Failed to initiate get location. Reason='${it.message}'.")
                        continuation.resume(null)
                    }
            } catch (e: Throwable) {
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }

    suspend fun geocodeLocation(location: LocationSnapshot): String? = suspendCancellableCoroutine { continuation ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder
                    .getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        if (address != null) {
                            continuation.resume(address.getAddressLine(0))
                        } else {
                            continuation.resume(null)
                        }
                    }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses!!.firstOrNull()!!
                continuation.resume(address.getAddressLine(0))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resume(null)
        }
    }

    fun redirectUserToLocationSystemSettings() {
        // Redirect the user to the location settings
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

}
