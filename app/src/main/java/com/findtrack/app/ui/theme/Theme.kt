package com.findtrack.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FindTrackColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenBadgeBg,
    onPrimaryContainer = GreenTextAccent,
    secondary = GreenTextAccent,
    onSecondary = Color.White,
    secondaryContainer = GreenSoftBg,
    onSecondaryContainer = GreenTextAccent,
    tertiary = UserBlue,
    background = PageBg,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    surfaceVariant = ChipDefaultBg,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    outlineVariant = DividerColor,
    error = DangerRed,
    onError = Color.White
)

/**
 * The approved design defines a light visual identity; the app keeps that
 * identity in both system modes so colors, contrast and hierarchy always
 * match the design.
 */
@Composable
fun FindTrackTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FindTrackColorScheme,
        typography = FindTrackTypography,
        content = content
    )
}
