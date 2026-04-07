package com.vennilay.kernvox.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val serverUrl: String = "",
    val apiKey: String = "",
)

class AppSettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            serverUrl = prefs[KEY_SERVER_URL] ?: "",
            apiKey = prefs[KEY_API_KEY] ?: "",
        )
    }

    suspend fun saveSettings(serverUrl: String, apiKey: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = serverUrl
            prefs[KEY_API_KEY] = apiKey
        }
    }

    suspend fun clearSettings() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
    }
}
