package com.findtrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.findtrack.app.ui.theme.CardWhite
import com.findtrack.app.ui.theme.DividerColor
import com.findtrack.app.ui.theme.GreenPrimary
import com.findtrack.app.ui.theme.TextTertiary

/** Destinations of the design's bottom navigation. */
enum class AppTab(val route: String, val label: String) {
    HOME("home", "Home"),
    DEVICES("devices", "Devices"),
    MAP("map", "Map"),
    HISTORY("history", "History")
}

private fun iconFor(tab: AppTab): ImageVector = when (tab) {
    AppTab.HOME -> Icons.Outlined.Home
    AppTab.DEVICES -> Icons.Outlined.PhoneAndroid
    AppTab.MAP -> Icons.Outlined.Place
    AppTab.HISTORY -> Icons.Outlined.History
}

/** The design's persistent bottom navigation bar. */
@Composable
fun FindTrackBottomBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CardWhite,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Keep labels above the gesture pill; white bar still extends
                // to the screen edge like the design.
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AppTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = iconFor(tab),
                        contentDescription = tab.label,
                        tint = if (isSelected) GreenPrimary else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = tab.label,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) GreenPrimary else TextTertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
