package com.findtrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeviceUnknown
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LaptopMac
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.findtrack.app.ble.DeviceIconType
import com.findtrack.app.ui.theme.CardWhite
import com.findtrack.app.ui.theme.FarBadgeBg
import com.findtrack.app.ui.theme.FarBadgeText
import com.findtrack.app.ui.theme.GreenBadgeBg
import com.findtrack.app.ui.theme.GreenPrimary
import com.findtrack.app.ui.theme.GreenSoftBg
import com.findtrack.app.ui.theme.GreenTextAccent
import com.findtrack.app.ui.theme.TextPrimary
import com.findtrack.app.ui.theme.TextSecondary

/** Maps a device family to the design's line icon. */
fun deviceIcon(type: DeviceIconType): ImageVector = when (type) {
    DeviceIconType.SPEAKER -> Icons.Outlined.Speaker
    DeviceIconType.PHONE -> Icons.Outlined.PhoneIphone
    DeviceIconType.WATCH -> Icons.Outlined.Watch
    DeviceIconType.EARBUDS -> Icons.Outlined.Headphones
    DeviceIconType.LAPTOP -> Icons.Outlined.LaptopMac
    DeviceIconType.TV -> Icons.Outlined.Tv
    DeviceIconType.TAG -> Icons.Outlined.Label
    DeviceIconType.UNKNOWN -> Icons.Outlined.DeviceUnknown
}

/** Soft green circle with a line icon — the design's device avatar. */
@Composable
fun DeviceIconBubble(
    type: DeviceIconType,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    background: Color = GreenSoftBg,
    tint: Color = GreenPrimary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = deviceIcon(type),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

/** Green "Nearby" pill badge. */
@Composable
fun NearbyBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(GreenBadgeBg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Nearby",
            color = GreenTextAccent,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Gray "Far" pill badge. */
@Composable
fun FarBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(FarBadgeBg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Far",
            color = FarBadgeText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Small green RSSI pill used by History rows: "-68 dBm". */
@Composable
fun RssiPill(rssi: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(GreenBadgeBg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "$rssi dBm",
            color = GreenTextAccent,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Standard white rounded card used across the design. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 1.dp
    ) {
        content()
    }
}

/**
 * Screen top bar with a back arrow (design pattern used by every pushed
 * screen) and optional trailing actions.
 */
@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            actions()
        }
        subtitle?.invoke()
    }
}

/** "•" separated caption row, e.g. "RSSI: -78 dBm • 2 min ago". */
@Composable
fun CaptionRow(
    first: String,
    second: String,
    modifier: Modifier = Modifier,
    firstColor: Color = TextSecondary,
    secondColor: Color = TextSecondary
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = first, style = MaterialTheme.typography.bodyMedium, color = firstColor)
        Text(text = "•", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = second, style = MaterialTheme.typography.bodyMedium, color = secondColor)
    }
}
