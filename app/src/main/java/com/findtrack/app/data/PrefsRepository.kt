package com.findtrack.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "settings")

/**
 * Small on-device preferences store. All user data stays on the phone —
 * the app has no accounts and no cloud sync.
 */
class PrefsRepository(private val context: Context) {

    private object Keys {
        val DEMO_MODE = booleanPreferencesKey("demo_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    /** Simulated devices for reviewing the UI without BLE hardware. */
    val demoMode: Flow<Boolean> = context.settingsStore.data.map { it[Keys.DEMO_MODE] ?: false }

    /** Proximity beeps while tracking. */
    val soundEnabled: Flow<Boolean> = context.settingsStore.data.map { it[Keys.SOUND_ENABLED] ?: true }

    suspend fun setDemoMode(enabled: Boolean) {
        context.settingsStore.edit { it[Keys.DEMO_MODE] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }
}
