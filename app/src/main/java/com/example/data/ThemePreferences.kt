package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hesabat_prefs")

class ThemePreferences(private val context: Context) {
  private val darkThemeKey = booleanPreferencesKey("is_dark_theme")

  val isDarkTheme: Flow<Boolean> =
      context.dataStore.data.map { preferences -> preferences[darkThemeKey] ?: true }

  suspend fun setDarkTheme(isDark: Boolean) {
    context.dataStore.edit { preferences -> preferences[darkThemeKey] = isDark }
  }
}
