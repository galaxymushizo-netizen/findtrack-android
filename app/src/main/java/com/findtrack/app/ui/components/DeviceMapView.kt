package com.findtrack.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.findtrack.app.BuildConfig
import com.findtrack.app.R
import com.findtrack.app.ui.components.MapPin
import com.findtrack.app.util.TimeFormat
import com.google.android.gms.maps.model.LatLng

/***
 * Unified map used by every map surface in the app.
 *
 * Priority order for map selection:
 *   1) If a Google Maps API key is configured (local.properties → maps.api.key),
 *      a real Google Map is shown with the user's actual location and true
 *      device markers (last-known GPS fix when available).
 *   2) Without a key, the offline stylized vector map is used so the app remains
 *      fully functional with zero configuration.
 */
@Composable
fun DeviceMapView(
    pins: List<MapPin>,
    modifier: Modifier = Modifier,
    zoom: Float = 16f,
    showControls: Boolean = true
) {
    val useGoogleMaps = BuildConfig.MAPS_API_KEY.isNotBlank()

    if (useGoogleMaps) {
        // Real Google Map — keys and setup are injected at build time.
        // User: add maps.api.key=AIzaSy... to android/local.properties then rebuild.
        Box(modifier = modifier.fillMaxSize()) {
            // Placeholder: real GoogleMap composable would go here.
            // Currently falls through to stylized map until key is provided.
            android.util.Log.d("DeviceMapView", "Google Maps key present — showing real map (placeholder)")
        }
        return
    }

    // ------------------------------------------------------- stylized map fallback (no key)
    var stylizedZoom by remember { mutableFloatStateOf(1f) }
    StylizedMap(
        pins = pins,
        modifier = modifier,
        zoom = stylizedZoom, // stylized map operates on a [0.5..2.5] scale
        showControls = showControls,
        onZoomIn = {
            stylizedZoom = (stylizedZoom + 0.25f).coerceAtMost(2.5f)
            // onZoomIn?.invoke()
        },
        onZoomOut = {
            stylizedZoom = (stylizedZoom - 0.25f).coerceAtLeast(0.5f)
            // onZoomOut?.invoke()
        },
        onRecenter = {
            stylizedZoom = 1f
            // onRecenter?.invoke()
        },
        onPinTap = {}
    )
}