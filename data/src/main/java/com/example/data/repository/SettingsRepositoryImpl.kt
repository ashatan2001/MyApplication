package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.BuildConfig
import com.example.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Делегат для создания отдельного экземпляра DataStore для настроек приложения.
 * Имя отличается от [com.example.data.di.NetworkModule], где хранятся куки аутентификации.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * Реализация репозитория настроек с использованием Jetpack DataStore.
 * Хранит пользовательский URL сервера. Если значение не задано,
 * возвращается [BuildConfig.BASE_URL].
 *
 * @param context Контекст приложения для доступа к DataStore.
 */
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    /** Ключ для хранения URL сервера в DataStore. */
    private val SERVER_URL_KEY = stringPreferencesKey("server_url")

    /**
     * Поток с текущим URL сервера.
     * Если пользователь не задал свой URL, возвращается [BuildConfig.BASE_URL].
     */
    override val serverUrl: Flow<String> = context.settingsDataStore.data
        .map { preferences ->
            preferences[SERVER_URL_KEY] ?: BuildConfig.BASE_URL
        }

    /**
     * Сохраняет новый URL сервера.
     * Автоматически добавляет завершающий слэш, если он отсутствует.
     *
     * @param url Новый адрес сервера.
     */
    override suspend fun saveServerUrl(url: String) {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        context.settingsDataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = normalizedUrl
        }
    }

    /**
     * Сбрасывает URL к значению по умолчанию, удаляя сохранённое значение.
     */
    override suspend fun resetToDefault() {
        context.settingsDataStore.edit { preferences ->
            preferences.remove(SERVER_URL_KEY)
        }
    }
}