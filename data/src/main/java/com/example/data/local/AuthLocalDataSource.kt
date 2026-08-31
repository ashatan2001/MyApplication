package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Расширение для создания единого экземпляра DataStore для всего приложения
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

@Singleton
class AuthLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val AUTH_STATE_KEY = booleanPreferencesKey("is_authenticated")

    /**
     * Сохраняет состояние авторизации.
     */
    suspend fun saveAuthState(isAuthenticated: Boolean) {
        context.authDataStore.edit { preferences ->
            preferences[AUTH_STATE_KEY] = isAuthenticated
        }
    }

    /**
     * Возвращает поток состояния авторизации.
     * По умолчанию (если ключа нет) возвращает false.
     */
    fun getAuthState(): Flow<Boolean> {
        return context.authDataStore.data.map { preferences ->
            preferences[AUTH_STATE_KEY] ?: false
        }
    }

    /**
     * Очищает данные сессии (удаляет ключ из хранилища).
     */
    suspend fun clearSession() {
        context.authDataStore.edit { preferences ->
            preferences.remove(AUTH_STATE_KEY)
        }
    }
}