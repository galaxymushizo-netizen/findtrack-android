package com.findtrack.app.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.findtrack.app.FindTrackApp
import com.findtrack.app.ble.DeviceIconType
import com.findtrack.app.ble.TrackedDevice
import com.findtrack.app.ble.TrackingController
import com.findtrack.app.ble.TrackingState
import com.findtrack.app.data.HistoryDao
import com.findtrack.app.data.HistoryEntity
import com.findtrack.app.data.SavedDeviceEntity
import com.findtrack.app.service.TrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/** Row model for the Devices list; merges live scan results with saved devices. */
data class DeviceListEntry(
    val address: String,
    val name: String,
    val rssi: Int?,
    val lastSeen: Long?,
    val isSaved: Boolean,
    val isDemo: Boolean,
    val iconType: DeviceIconType,
    val distanceM: Double?,
    val isNearby: Boolean
) {
    companion object {
        fun from(device: TrackedDevice, saved: Boolean) = DeviceListEntry(
            address = device.address,
            name = device.name,
            rssi = device.rssi,
            lastSeen = device.lastSeen,
            isSaved = saved,
            isDemo = device.isDemo,
            iconType = device.iconType,
            distanceM = device.distanceM,
            isNearby = device.isNearby
        )

        fun fromSaved(entity: SavedDeviceEntity) = DeviceListEntry(
            address = entity.address,
            name = entity.name,
            rssi = null,
            lastSeen = null,
            isSaved = true,
            isDemo = entity.address.startsWith("demo:"),
            iconType = com.findtrack.app.ble.classifyDevice(entity.name),
            distanceM = null,
            isNearby = false
        )
    }
}

/**
 * Single shared ViewModel for all screens: owns merged device state, saved
 * devices, history, selection and tracking session wiring.
 */
class FindTrackViewModel(app: Application) : AndroidViewModel(app) {

    private val graph = FindTrackApp.graph
    private val scanner = graph.scanner
    private val prefs = graph.prefs
    private val location = graph.location
    private val database = graph.database
    private val deviceDao = database.deviceDao()
    private val historyDao: HistoryDao = database.historyDao()

    // ---------------------------------------------------------------- BLE

    val bluetoothAvailable: Boolean get() = scanner.bluetoothAvailable
    val scanning: StateFlow<Boolean> = scanner.scanning
    val bluetoothOn: StateFlow<Boolean> = scanner.bluetoothOn
    val scanError: StateFlow<String?> = scanner.scanError

    /** Merged live + saved devices, strongest first. */
    val devices: StateFlow<List<DeviceListEntry>> =
        combine(scanner.devices, deviceDao.observeSaved()) { live, saved ->
            val savedMap = saved.associateBy { it.address }
            live.map { DeviceListEntry.from(it, savedMap.containsKey(it.address)) } +
                saved.filter { s -> live.none { it.address == s.address } }
                    .map { DeviceListEntry.fromSaved(it) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val savedCount: StateFlow<Int> = deviceDao.observeSaved()
        .map { list -> list.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val nearbyCount: StateFlow<Int> = devices
        .map { list -> list.count { it.isNearby } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // ------------------------------------------------------------ history

    val history: StateFlow<List<HistoryEntity>> = historyDao.observeRecent(200)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val historyCount: StateFlow<Int> = historyDao.observeCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // --------------------------------------------------------- selection

    private val _selectedAddress = MutableStateFlow<String?>(null)
    val selectedAddress: StateFlow<String?> = _selectedAddress.asStateFlow()

    /** Last-known entry cache so Map/Home keep working between scan batches. */
    private val _knownDevices = MutableStateFlow<Map<String, DeviceListEntry>>(emptyMap())

    val selectedDevice: StateFlow<DeviceListEntry?> =
        combine(_selectedAddress, _knownDevices) { address, known ->
            address?.let { known[it] }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // ----------------------------------------------------- tracking/prefs

    val tracking: StateFlow<TrackingState?> = TrackingController.state
    val trackingAddress: StateFlow<String?> = TrackingController.activeAddress

    val demoMode: StateFlow<Boolean> = prefs.demoMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val soundEnabled: StateFlow<Boolean> = prefs.soundEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** User's live position for the real Google Map (null until fixed). */
    val userLatLng: StateFlow<com.google.android.gms.maps.model.LatLng?> =
        location.lastFix
            .map { fix -> fix?.let { com.google.android.gms.maps.model.LatLng(it.lat, it.lon) } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Last known real coordinates per device address (from History). */
    fun lastKnownCoordinates(address: String): Pair<Double, Double>? =
        history.value.firstOrNull { it.address == address && it.lat != null && it.lon != null }
            ?.let { it.lat!! to it.lon!! }

    init {
        // Keep scanning while any screen is open.
        scanner.acquire(this)
        if (location.hasPermission()) location.prime()

        viewModelScope.launch {
            devices.debounce(300).collect { list ->
                _knownDevices.value = list.associateBy { it.address }
            }
        }

        // Record history for saved devices while the app is open (10 min cooldown
        // is applied inside LocationRepository.recordSighting).
        viewModelScope.launch {
            combine(scanner.devices, deviceDao.observeSaved()) { live, saved ->
                val savedAddresses = saved.map { it.address }.toSet()
                live.filter { it.address in savedAddresses }
            }.distinctUntilChanged().debounce(5_000).collect { list ->
                if (hasLocationPermission()) {
                    list.forEach { location.recordSighting(historyDao, it) }
                }
            }
        }

        // Seed the design's sample history when demo mode is switched on.
        viewModelScope.launch {
            combine(prefs.demoMode, historyDao.observeCount()) { demo, count -> demo to count }
                .distinctUntilChanged().collect { (demo, count) ->
                    if (demo && count == 0) seedDemoHistory()
                }
        }
    }

    // ------------------------------------------------------------ actions

    fun refresh() {
        scanner.restart()
    }

    fun clearScanError() {
        scanner.clearScanError()
    }

    fun select(address: String) {
        _selectedAddress.value = address
    }

    fun startTracking(context: Context, entry: DeviceListEntry) {
        viewModelScope.launch {
            deviceDao.upsert(
                SavedDeviceEntity(entry.address, entry.name, System.currentTimeMillis())
            )
        }
        select(entry.address)
        TrackingService.start(context, entry.address, entry.name)
    }

    fun stopTracking(context: Context) {
        TrackingService.stop(context)
    }

    fun toggleSaved(entry: DeviceListEntry) {
        viewModelScope.launch {
            if (entry.isSaved) deviceDao.delete(entry.address)
            else deviceDao.upsert(
                SavedDeviceEntity(entry.address, entry.name, System.currentTimeMillis())
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.clear() }
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDemoMode(enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setSoundEnabled(enabled) }
    }

    // -------------------------------------------------------- permissions

    fun hasAllPermissions(context: Context): Boolean =
        missingPermissions(context).isEmpty()

    fun missingPermissions(context: Context): List<String> =
        requiredPermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

    fun hasLocationPermission(): Boolean = location.hasPermission()

    /** Called after a permission request completes to warm up location fixes. */
    fun onLocationPermissionPossiblyGranted() {
        if (location.hasPermission()) location.prime()
    }

    fun requiredPermissions(): Array<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        // GPS is used to save real "last seen" places in History.
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.distinct().toTypedArray()

    fun permissionRationale(context: Context): String {
        val missing = missingPermissions(context)
        return when {
            missing.isEmpty() -> "All permissions granted."
            missing.any { it.contains("BLUETOOTH") } ->
                "Bluetooth access is required to scan for nearby devices. " +
                    "FindTrack never derives your location from Bluetooth."
            else ->
                "Location access is used to remember where your devices were last seen. " +
                    "Everything stays on this phone."
        }
    }

    // ---------------------------------------------------------------- demo

    /** The design's sample devices, used for demo history seeding. */
    private fun seedDemoHistory() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val samples = listOf(
                Triple("demo:00:00:00:00:01", "JBL Flip 6", -68),
                Triple("demo:00:00:00:00:02", "Galaxy S23", -65),
                Triple("demo:00:00:00:00:03", "Apple Watch", -82),
                Triple("demo:00:00:00:00:04", "AirPods Pro", -90),
                Triple("demo:00:00:00:00:05", "JBL Charge 5", -102)
            )
            val places = listOf(
                "Riverside Park, NY" to Pair(40.8030, -73.9630),
                "Central Park, NY" to Pair(40.7829, -73.9654),
                "5th Ave, NY" to Pair(40.7740, -73.9650),
                "Times Square, NY" to Pair(40.7580, -73.9855),
                "Hudson River Park, NY" to Pair(40.7335, -74.0090)
            )
            val times = listOf(
                clockToday(9, 37, now),
                clockToday(8, 14, now),
                clockYesterday(18, 32, now),
                clockYesterday(15, 21, now),
                clockYesterday(23, 3, now)
            )
            samples.forEachIndexed { i, (address, devName, rssi) ->
                historyDao.insert(
                    HistoryEntity(
                        address = address,
                        name = devName,
                        rssi = rssi,
                        lat = places[i].second.first,
                        lon = places[i].second.second,
                        place = places[i].first,
                        timestamp = times[i],
                        isDemo = true
                    )
                )
            }
        }
    }

    private fun clockToday(hour: Int, minute: Int, now: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis > now) add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

    private fun clockYesterday(hour: Int, minute: Int, now: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    override fun onCleared() {
        scanner.release(this)
        super.onCleared()
    }
}
