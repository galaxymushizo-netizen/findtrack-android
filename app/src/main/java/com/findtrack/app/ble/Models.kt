package com.findtrack.app.ble

/** Icon families used by the design's device rows and map pins. */
enum class DeviceIconType { SPEAKER, PHONE, WATCH, EARBUDS, LAPTOP, TV, TAG, UNKNOWN }

/**
 * A BLE device observed by the scanner (or produced by the clearly-labeled
 * demo/simulation engine when demo mode is enabled).
 */
data class TrackedDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val lastSeen: Long,
    val isDemo: Boolean = false
) {
    val distanceM: Double
        get() = DistanceEstimator.distanceFromRssi(rssi)

    val iconType: DeviceIconType
        get() = classifyDevice(name)

    /** "Nearby" per the design: within 30 m estimated distance. */
    val isNearby: Boolean
        get() = distanceM <= DistanceEstimator.NEARBY_THRESHOLD_M

    val signalLevel: SignalLevel
        get() = DistanceEstimator.signalLevel(rssi)
}

/**
 * Best-effort device-family classification from the advertised/cached name.
 * Real devices expose rich metadata only after a GATT connection, which would
 * cost battery and time; name heuristics are the standard approach for
 * scanner-style apps.
 */
fun classifyDevice(name: String): DeviceIconType {
    val n = name.lowercase()
    return when {
        listOf("watch", "band", "galaxy fit", "mi band").any { n.contains(it) } -> DeviceIconType.WATCH
        listOf("airpods", "buds", "pods", "earphone", "headphone", "earbud", "life p")
            .any { n.contains(it) } -> DeviceIconType.EARBUDS
        listOf("flip", "charge", "speaker", "jbl", "bose", "sound", "beat", "homepod", "mega")
            .any { n.contains(it) } -> DeviceIconType.SPEAKER
        listOf("iphone", "galaxy", "pixel", "phone", "oneplus", "redmi", "note ", "s23", "s24")
            .any { n.contains(it) } -> DeviceIconType.PHONE
        listOf("macbook", "laptop", "computer", "thinkpad", "desktop").any { n.contains(it) } -> DeviceIconType.LAPTOP
        listOf("tv", "chromecast", "fire stick", "roku", "shield").any { n.contains(it) } -> DeviceIconType.TV
        listOf("tile", "tag", "tracker", "chipolo", "airtag").any { n.contains(it) } -> DeviceIconType.TAG
        n.isBlank() || n == "unknown device" -> DeviceIconType.TAG
        else -> DeviceIconType.UNKNOWN
    }
}
