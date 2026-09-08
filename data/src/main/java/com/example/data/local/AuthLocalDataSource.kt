package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_data")

@Singleton
class AuthLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val AUTH_STATE_KEY = booleanPreferencesKey("is_authenticated")
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    suspend fun saveAuthState(isAuthenticated: Boolean) {
        context.authDataStore.edit { it[AUTH_STATE_KEY] = isAuthenticated }
    }

    fun getAuthState(): Flow<Boolean> =
        context.authDataStore.data.map { it[AUTH_STATE_KEY] ?: false }

    suspend fun saveAuthToken(token: String) {
        context.authDataStore.edit { it[AUTH_TOKEN_KEY] = token }
    }

    fun getAuthToken(): Flow<String?> =
        context.authDataStore.data.map { it[AUTH_TOKEN_KEY] }

    suspend fun clearSession() {
        context.authDataStore.edit {
            it.remove(AUTH_STATE_KEY)
            it.remove(AUTH_TOKEN_KEY)
        }
    }
}