package com.findtrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.findtrack.app.ble.DistanceEstimator
import com.findtrack.app.ble.TrackStatus
import com.findtrack.app.ui.FindTrackViewModel
import com.findtrack.app.ui.components.BackTopBar
import com.findtrack.app.ui.components.RadarAnimation
import com.findtrack.app.ui.components.SectionCard
import com.findtrack.app.ui.theme.CardWhite
import com.findtrack.app.ui.theme.DangerRed
import com.findtrack.app.ui.theme.DividerColor
import com.findtrack.app.ui.theme.GreenBadgeBg
import com.findtrack.app.ui.theme.GreenPrimary
import com.findtrack.app.ui.theme.GreenTextAccent
import com.findtrack.app.ui.theme.PageBg
import com.findtrack.app.ui.theme.TextPrimary
import com.findtrack.app.ui.theme.TextSecondary
import com.findtrack.app.ui.theme.TextTertiary
import com.findtrack.app.util.TimeFormat

/**
 * Tracking — live proximity screen: radar animation, signal meter and
 * RSSI / distance / last-seen readouts with a Stop Tracking action.
 */
@Composable
fun TrackingScreen(
    vm: FindTrackViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    contentPadding: PaddingValues
) {
    val tracking by vm.tracking.collectAsStateWithLifecycle()
    val soundOn by vm.soundEnabled.collectAsStateWithLifecycle()
    val devices by vm.devices.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val state = tracking
    val entry = state?.let { s -> devices.firstOrNull { it.address == s.address } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackTopBar(
            title = "Tracking",
            onBack = onBack,
            actions = {
                IconButton(onClick = { vm.setSoundEnabled(!soundOn) }) {
                    Icon(
                        imageVector = if (soundOn) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff,
                        contentDescription = if (soundOn) "Mute proximity beeps" else "Enable proximity beeps",
                        tint = TextSecondary
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
        )

        if (state == null) {
            // Session not started (or just stopped) — offer a way back.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No active tracking session",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Open a device and tap Start Tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            return@Column
        }

        // ------------------------------------------------- radar hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            RadarAnimation(
                iconType = entry?.iconType ?: com.findtrack.app.ble.DeviceIconType.TAG,
                active = state.status != TrackStatus.LOST
            )
        }

        Text(
            text = "Tracking ${state.name}",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(6.dp))

        val statusTitle = when (state.status) {
            TrackStatus.STRONGER -> "Signal getting stronger"
            TrackStatus.WEAKER -> "Signal is weakening"
            TrackStatus.STEADY -> "Signal is steady"
            TrackStatus.LOST -> "Signal lost"
            TrackStatus.SEARCHING -> "Searching for signal"
        }
        val statusSubtitle = when (state.status) {
            TrackStatus.STRONGER -> "You're getting closer."
            TrackStatus.WEAKER -> "You may be moving away."
            TrackStatus.STEADY -> "Hold steady and watch the meter."
            TrackStatus.LOST -> "We'll keep looking. Last position saved."
            TrackStatus.SEARCHING -> "Waiting for the first reading…"
        }
        Text(
            text = statusTitle,
            style = MaterialTheme.typography.labelLarge,
            color = if (state.status == TrackStatus.LOST) DangerRed else GreenTextAccent,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = statusSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 2.dp, bottom = 14.dp)
        )

        // ------------------------------------------------ signal meter
        LinearProgressIndicator(
            progress = { DistanceEstimator.strengthProgress(state.rssi) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = GreenPrimary,
            trackColor = GreenBadgeBg
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------------------------------------------------- readouts
        SectionCard {
            Column {
                ReadoutRow(
                    icon = { Icon(Icons.Outlined.SignalCellularAlt, null, tint = GreenPrimary) },
                    label = "RSSI",
                    value = state.rssi?.let { "$it dBm" } ?: "—",
                    trailing = state.rssi?.let {
                        "[${DistanceEstimator.signalLevel(it).label}]"
                    }
                )
                Divider()
                ReadoutRow(
                    icon = { Icon(Icons.Outlined.LocationOn, null, tint = GreenPrimary) },
                    label = "Distance (approx.)",
                    value = state.distanceM?.let { DistanceEstimator.formatDistance(it) } ?: "—"
                )
                Divider()
                ReadoutRow(
                    icon = { Icon(Icons.Outlined.History, null, tint = GreenPrimary) },
                    label = "Last seen",
                    value = TimeFormat.absolute(state.lastSeen)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ----------------------------------------------- stop action
        OutlinedButton(
            onClick = { vm.stopTracking(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DividerColor),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Icon(
                imageVector = Icons.Outlined.Stop,
                contentDescription = null,
                tint = GreenPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Stop Tracking", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (!soundOn) {
            Text(
                text = "Proximity sounds are muted",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ReadoutRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    trailing: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall,
                color = GreenTextAccent
            )
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(DividerColor)
    )
}
