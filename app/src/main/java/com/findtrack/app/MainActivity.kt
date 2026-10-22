package com.findtrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.findtrack.app.ui.AppNav
import com.findtrack.app.ui.theme.FindTrackTheme
import com.findtrack.app.ui.theme.PageBg

/**
 * Single-activity host for the whole app. The approved design is delivered
 * as a Compose UI inside this activity.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PageBg
                ) {
                    AppNav()
                }
            }
        }
    }
}
