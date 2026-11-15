package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hesabat_settings")

class ThemePreferences(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("is_dark_glass")

    val isDarkGlassFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[darkModeKey] ?: true }

    suspend fun setDarkGlass(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[darkModeKey] = isDark
        }
    }
}
