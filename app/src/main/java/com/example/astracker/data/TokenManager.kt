package com.example.astracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension on Context — one DataStore per app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "astracker_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    /** Persist JWT token */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    /** Read JWT token (null if not set) */
    suspend fun getToken(): String? =
        context.dataStore.data
            .map { prefs -> prefs[TOKEN_KEY] }
            .first()

    /** Remove token on logout */
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}
