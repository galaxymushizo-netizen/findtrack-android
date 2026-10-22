package com.findtrack.app.ble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TrackStatus { SEARCHING, STRONGER, WEAKER, STEADY, LOST }

/** Live state of an active tracking session, shared between UI and service. */
data class TrackingState(
    val address: String,
    val name: String,
    val rssi: Int?,
    val distanceM: Double?,
    val lastSeen: Long,
    val status: TrackStatus,
    val soundOn: Boolean,
    val maxRssi: Int?
)

/**
 * Process-wide holder for the active tracking session so the foreground
 * [com.findtrack.app.service.TrackingService] and the UI observe one source
 * of truth.
 */
object TrackingController {

    private val _state = MutableStateFlow<TrackingState?>(null)
    val state: StateFlow<TrackingState?> = _state.asStateFlow()

    private val _activeAddress = MutableStateFlow<String?>(null)
    val activeAddress: StateFlow<String?> = _activeAddress.asStateFlow()

    fun isTracking(address: String): Boolean = _activeAddress.value == address

    fun update(state: TrackingState?) {
        _state.value = state
        _activeAddress.value = state?.address
    }

    fun currentAddress(): String? = _activeAddress.value
}
