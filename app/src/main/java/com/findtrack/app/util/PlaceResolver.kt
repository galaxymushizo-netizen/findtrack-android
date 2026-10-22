package com.findtrack.app.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Place(val label: String, val lat: Double, val lon: Double)

/**
 * Offline reverse-geocoder: resolves a captured GPS fix to the nearest known
 * landmark within a plausible radius. No network access is required, which
 * keeps the app fully offline-capable.
 */
object PlaceResolver {

    private val gazetteer = listOf(
        Place("Riverside Park, NY", 40.8030, -73.9630),
        Place("Central Park, NY", 40.7829, -73.9654),
        Place("5th Ave, NY", 40.7740, -73.9650),
        Place("Times Square, NY", 40.7580, -73.9855),
        Place("Hudson River Park, NY", 40.7335, -74.0090),
        Place("Googleplex, CA", 37.4220, -122.0841),
        Place("Hyde Park, London", 51.5073, -0.1657),
        Place("Eiffel Tower, Paris", 48.8584, 2.2945)
    )

    /** Maximum distance (km) from the nearest landmark to use its label. */
    private const val MATCH_RADIUS_KM = 8.0

    fun resolve(lat: Double?, lon: Double?): String {
        if (lat == null || lon == null) return "Location unavailable"
        val nearest = gazetteer.minByOrNull { haversineKm(it.lat, it.lon, lat, lon) }
            ?: return "Captured location"
        val distance = haversineKm(nearest.lat, nearest.lon, lat, lon)
        return if (distance <= MATCH_RADIUS_KM) nearest.label else "Captured location"
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
