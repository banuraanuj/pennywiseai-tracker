package com.pennywiseai.tracker.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to fetch device geolocation (latitude, longitude) securely when SMS is received or processed.
 */
@Singleton
class LocationManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Gets the current device geolocation (latitude, longitude) if location permissions are granted.
     * Returns null if permissions are not granted or no location is available.
     */
    fun getCurrentLocation(): Pair<Double, Double>? {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return null
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            
            var bestLocation: Location? = null
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }

            return bestLocation?.let { Pair(it.latitude, it.longitude) }
        } catch (e: Exception) {
            return null
        }
    }
}
