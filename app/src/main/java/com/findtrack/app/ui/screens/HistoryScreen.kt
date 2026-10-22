package com.findtrack.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.findtrack.app.ble.classifyDevice
import com.findtrack.app.data.HistoryEntity
import com.findtrack.app.ui.FindTrackViewModel
import com.findtrack.app.ui.components.DeviceIconBubble
import com.findtrack.app.ui.components.RssiPill
import com.findtrack.app.ui.components.SectionCard
import com.findtrack.app.ui.theme.CardWhite
import com.findtrack.app.ui.theme.DividerColor
import com.findtrack.app.ui.theme.GreenInfoBg
import com.findtrack.app.ui.theme.GreenPrimary
import com.findtrack.app.ui.theme.GreenSoftBg
import com.findtrack.app.ui.theme.GreenTextAccent
import com.findtrack.app.ui.theme.PageBg
import com.findtrack.app.ui.theme.TextPrimary
import com.findtrack.app.ui.theme.TextSecondary
import com.findtrack.app.ui.theme.TextTertiary
import com.findtrack.app.util.TimeFormat

/**
 * History — chronological record of where each saved/tracked device was last
 * seen, with a confirmation-protected clear action and the design's info card.
 */
@Composable
fun HistoryScreen(
    vm: FindTrackViewModel,
    onOpenDevice: (String) -> Unit,
    contentPadding: PaddingValues
) {
    val history by vm.history.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Recent device locations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            if (history.isNotEmpty()) {
                IconButton(onClick = { confirmClear = true }) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear history",
                        tint = TextSecondary
                    )
                }
            }
        }

        if (history.isEmpty()) {
            EmptyHistoryState()
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                SectionCard(modifier = Modifier.padding(top = 8.dp)) {
                    Column {
                        history.forEachIndexed { index, entry ->
                            HistoryRow(entry = entry, onClick = { onOpenDevice(entry.address) })
                            if (index != history.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                        .height(1.dp)
                                        .background(DividerColor)
                                )
                            }
                        }
                    }
                }
            }
            item {
                // ------------------------------------------ info card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenInfoBg)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "Keep your device nearby",
                            style = MaterialTheme.typography.labelLarge,
                            color = GreenTextAccent
                        )
                        Text(
                            text = "We'll save the last known location when the device disconnects.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenTextAccent
                        )
                    }
                }
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }

    if (confirmClear) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear history?") },
            text = { Text("This removes all recorded device locations from this phone. This cannot be undone.") },
            confirmButton = {
                Text(
                    text = "Clear",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            vm.clearHistory()
                            confirmClear = false
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { confirmClear = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        )
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeviceIconBubble(type = classifyDevice(entry.name), size = 42.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = TimeFormat.absolute(entry.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = entry.place,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
        RssiPill(rssi = entry.rssi)
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextTertiary
        )
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GreenSoftBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Place,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.size(14.dp))
        Text(
            text = "No history yet",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Locations of your saved devices will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
