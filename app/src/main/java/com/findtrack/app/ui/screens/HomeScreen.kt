package com.findtrack.app.ui.screens

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.findtrack.app.ui.DeviceListEntry
import com.findtrack.app.ui.FindTrackViewModel
import com.findtrack.app.ui.components.DeviceIconBubble
import com.findtrack.app.ui.components.DeviceMapView
import com.findtrack.app.ui.components.MapPin
import com.findtrack.app.ui.components.NearbyBadge
import com.findtrack.app.ui.components.SectionCard
import com.findtrack.app.ui.components.StylizedMap
import com.findtrack.app.ui.theme.GreenBadgeBg
import com.findtrack.app.ui.theme.GreenPrimary
import com.findtrack.app.ui.theme.GreenSoftBg
import com.findtrack.app.ui.theme.GreenTextAccent
import com.findtrack.app.ui.theme.PageBg
import com.findtrack.app.ui.theme.TextPrimary
import com.findtrack.app.ui.theme.TextSecondary
import com.findtrack.app.ui.theme.TextTertiary
import com.findtrack.app.util.TimeFormat

/**
 * Home — the design's landing screen: brand header, live scanning card,
 * map with device pins and the strongest/selected device quick card.
 */
@Composable
fun HomeScreen(
    vm: FindTrackViewModel,
    onOpenDevices: () -> Unit,
    onOpenDevice: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onRequestPermissions: () -> Unit,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    val devices by vm.devices.collectAsStateWithLifecycle()
    val scanning by vm.scanning.collectAsStateWithLifecycle()
    val bluetoothOn by vm.bluetoothOn.collectAsStateWithLifecycle()
    val bluetoothAvailable = vm.bluetoothAvailable
    val selectedDevice by vm.selectedDevice.collectAsStateWithLifecycle()
    val selectedAddress by vm.selectedAddress.collectAsStateWithLifecycle()
    val demoMode by vm.demoMode.collectAsStateWithLifecycle()
    val missingPermissions = vm.missingPermissions(context)

    var zoom by remember { mutableFloatStateOf(1f) }
    val userLatLng by vm.userLatLng.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()

    // The quick card shows the explicit selection or the strongest device.
    val featured: DeviceListEntry? = selectedDevice
        ?: devices.firstOrNull { it.lastSeen != null }

    val pins = remember(devices, selectedAddress, history) {
        devices
            .filter { !it.isDemo && it.lastSeen != null && it.distanceM != null }
            .map { entry ->
                val fix = history.firstOrNull {
                    it.address == entry.address && it.lat != null && it.lon != null
                }
                MapPin(
                    address = entry.address,
                    iconType = entry.iconType,
                    distanceM = entry.distanceM ?: 10.0,
                    selected = entry.address == selectedAddress || (selectedAddress == null && featured?.address == entry.address),
                    lat = fix?.lat,
                    lon = fix?.lon
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
    ) {
        // ------------------------------------------------------- header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "FindTrack",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Find your devices. Stay connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary
                )
            }
        }

        // -------------------------------------------- permission banner
        if (missingPermissions.isNotEmpty()) {
            SectionCard(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GreenBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bluetooth,
                            tint = GreenPrimary,
                            contentDescription = null
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Access needed",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Grant Bluetooth & location access to discover nearby devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Grant",
                        style = MaterialTheme.typography.labelLarge,
                        color = GreenPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(GreenSoftBg)
                            .clickable { onRequestPermissions() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // ------------------------------------------------ scanning card
        // (Shown only after access is granted — the banner above owns the CTA,
        // so the two never duplicate "Access needed".)
        if (missingPermissions.isEmpty()) {
            SectionCard(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (missingPermissions.isEmpty()) onOpenDevices() else onRequestPermissions()
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GreenSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bluetooth,
                        contentDescription = null,
                        tint = GreenPrimary
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    val title = when {
                        missingPermissions.isNotEmpty() -> "Access needed"
                        !bluetoothAvailable -> "Bluetooth unavailable"
                        !bluetoothOn -> "Bluetooth is off"
                        scanning -> "Scanning for devices…"
                        else -> "Scanning paused"
                    }
                    val subtitle = when {
                        missingPermissions.isNotEmpty() -> "Tap to grant access"
                        !bluetoothAvailable -> "This device has no BLE hardware — try demo mode"
                        !bluetoothOn -> "Turn on Bluetooth to find your devices"
                        else -> {
                            val count = devices.count { it.lastSeen != null }
                            val near = devices.count { it.isNearby && it.lastSeen != null }
                            if (count == 0) "Looking for nearby devices…"
                            else "$near devices nearby • $count found"
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (scanning && bluetoothOn && missingPermissions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = TextTertiary
                )
            }
            }
        }

        // --------------------------------------------------- quick map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            DeviceMapView(
                pins = pins,
                modifier = Modifier.fillMaxSize(),
                zoom = 16f,
                showControls = true,
                userLocation = userLatLng,
                selectedAddress = selectedAddress,
                onPinTap = { vm.select(it) }
            )
            if (demoMode) {
                Text(
                    text = "Demo mode — simulated devices",
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenTextAccent,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(GreenBadgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            if (devices.isEmpty() && missingPermissions.isEmpty() && bluetoothOn) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SignalCellularAlt,
                        contentDescription = null,
                        tint = TextTertiary
                    )
                    Text(
                        text = "No devices found yet",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Keep your devices powered and nearby",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // ------------------------------------------- featured device card
        if (featured != null) {
            val device = featured
            SectionCard(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDevice(device.address) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeviceIconBubble(type = device.iconType, size = 42.dp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (device.isNearby) NearbyBadge()
                        }
                        val rssi = device.rssi
                        val seen = device.lastSeen
                        val detail = buildList {
                            if (rssi != null) add("RSSI: $rssi dBm")
                            if (seen != null) add(TimeFormat.relative(seen))
                        }.joinToString("  •  ")
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = TextTertiary
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}
