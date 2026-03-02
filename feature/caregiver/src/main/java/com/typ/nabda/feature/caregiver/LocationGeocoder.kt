package com.typ.nabda.feature.caregiver

import android.content.Context
import android.location.Geocoder
import com.typ.nabda.core.model.LocationSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationGeocoder(private val context: Context) {

    suspend fun geocode(snapshot: LocationSnapshot?): String = withContext(Dispatchers.IO) {
        if (snapshot == null) return@withContext "Location unavailable"

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(snapshot.latitude, snapshot.longitude, 1)
            val address = addresses?.firstOrNull()

            if (address != null) {
                val country = address.countryName ?: ""
                val city = address.locality ?: address.adminArea ?: ""
                val area = address.subLocality ?: address.thoroughfare ?: ""

                listOf(country, city, area)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            "Location unavailable"
        }
    }
}
