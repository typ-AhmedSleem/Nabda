package com.typ.nabda.feature.caregiver

import android.content.Context
import android.location.Geocoder
import com.typ.nabda.core.model.LocationSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationGeocoder(private val context: Context) {

    private val cache = mutableMapOf<Pair<Double, Double>, String>()

    suspend fun geocode(snapshot: LocationSnapshot?): String {
        if (snapshot == null) return context.getString(R.string.location_unavailable)

        // Check cache first
        val coords = snapshot.latitude to snapshot.longitude
        cache[coords]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(snapshot.latitude, snapshot.longitude, 1)
                val address = addresses?.firstOrNull()

                if (address != null) {
                    val country = address.countryName ?: ""
                    val city = address.locality ?: address.adminArea ?: ""
                    val area = address.subLocality ?: address.thoroughfare ?: ""

                    val result = listOf(country, city, area)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    cache[coords] = result
                    result
                } else {
                    context.getString(R.string.unknown_location)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                context.getString(R.string.location_unavailable)
            }
        }
    }
}
