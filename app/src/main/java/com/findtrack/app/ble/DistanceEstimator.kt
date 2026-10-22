package com.findtrack.app.ble

import kotlin.math.pow
import kotlin.math.roundToInt

enum class SignalLevel(val label: String) {
    VERY_STRONG("very strong"),
    STRONG("strong"),
    MODERATE("moderate"),
    WEAK("weak")
}

/**
 * Estimates physical distance from BLE signal strength using the standard
 * log-distance path-loss model:
 *
 *   d = 10 ^ ((TxPower - RSSI) / (10 * n))
 *
 * TxPower is the calibrated RSSI at 1 meter (-59 dBm is typical for BLE
 * class-2 devices) and n is the path-loss exponent (2.2 ~ typical indoor
 * environment with mild obstructions).
 */
object DistanceEstimator {

    /** Calibrated RSSI at 1 meter. */
    const val TX_POWER_AT_1M = -59.0

    /** Path-loss exponent: 2.0 free space, 2.2-3.5 indoors. */
    const val PATH_LOSS_EXPONENT = 2.2

    /** Devices closer than this are classified as "Nearby" per the design. */
    const val NEARBY_THRESHOLD_M = 30.0

    fun distanceFromRssi(
        rssi: Int,
        txPower: Double = TX_POWER_AT_1M,
        exponent: Double = PATH_LOSS_EXPONENT
    ): Double {
        if (rssi == 0) return MAX_DISTANCE_M // invalid reading guard
        val exponentSafe = if (exponent <= 0) 2.2 else exponent
        val ratio = (txPower - rssi) / (10.0 * exponentSafe)
        val meters = 10.0.pow(ratio)
        return meters.coerceIn(MIN_DISTANCE_M, MAX_DISTANCE_M)
    }

    fun signalLevel(rssi: Int): SignalLevel = when {
        rssi >= -60 -> SignalLevel.VERY_STRONG
        rssi >= -67 -> SignalLevel.STRONG
        rssi >= -80 -> SignalLevel.MODERATE
        else -> SignalLevel.WEAK
    }

    /** 0.05f..1f progress used by the design's signal meter. */
    fun strengthProgress(rssi: Int?): Float {
        if (rssi == null) return 0.05f
        return ((rssi + 100) / 50f).coerceIn(0.05f, 1f)
    }

    /** Human readable distance per the design: "≈ 12 m" / "≈ 1.2 km". */
    fun formatDistance(meters: Double): String = when {
        meters >= 1000 -> "≈ %.1f km".format(meters / 1000.0)
        else -> "≈ ${meters.roundToInt()} m"
    }

    const val MIN_DISTANCE_M = 0.3
    const val MAX_DISTANCE_M = 99.0
}
