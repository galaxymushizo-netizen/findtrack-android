package com.findtrack.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.findtrack.app.ui.theme.GreenPrimary
import com.findtrack.app.ui.theme.GreenRadarFill
import com.findtrack.app.ui.theme.GreenRadarRing
import com.findtrack.app.ble.DeviceIconType

/**
 * The tracking screen's radar: expanding pulse rings around the device pin,
 * matching the approved design's "pinging" visual.
 */
@Composable
fun RadarAnimation(
    iconType: DeviceIconType,
    modifier: Modifier = Modifier,
    outerDiameter: Dp = 264.dp,
    centerBubble: Dp = 96.dp,
    active: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "radar")
    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "radar1"
    )
    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, delayMillis = 850, easing = LinearEasing), RepeatMode.Restart),
        label = "radar2"
    )
    val phase3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, delayMillis = 1700, easing = LinearEasing), RepeatMode.Restart),
        label = "radar3"
    )

    Box(modifier = modifier.size(outerDiameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(outerDiameter)) {
            val maxRadius = size.minDimension / 2f
            // Static base rings.
            drawCircle(
                color = GreenRadarFill,
                radius = maxRadius * 0.96f
            )
            drawCircle(
                color = GreenRadarRing,
                radius = maxRadius * 0.66f,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = GreenRadarRing,
                radius = maxRadius * 0.36f,
                style = Stroke(width = 2.dp.toPx())
            )
            if (active) {
                // Three expanding pulses.
                listOf(phase1, phase2, phase3).forEach { p ->
                    val radius = maxRadius * (0.22f + 0.74f * p)
                    val alpha = (1f - p) * 0.55f
                    drawCircle(
                        color = GreenPrimary.copy(alpha = alpha.coerceIn(0f, 1f)),
                        radius = radius,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center pin bubble with the device icon (design's hero element).
        Box(
            modifier = Modifier
                .size(centerBubble)
                .clip(CircleShape)
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = deviceIcon(iconType),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(centerBubble * 0.42f)
            )
        }
    }
}
