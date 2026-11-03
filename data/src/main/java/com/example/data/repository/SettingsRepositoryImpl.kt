package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.BuildConfig
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.ServerApiException
import com.example.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
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

    /**
     * Проверяет доступность сервера по указанному URL.
     * Использует стандартный [HttpURLConnection] для выполнения HEAD-запроса,
     * что позволяет избежать необходимости модификации Hilt-модуля для инъекции OkHttpClient.
     *
     * @param url Адрес сервера для проверки.
     */
    override suspend fun testConnection(url: String) = withContext(Dispatchers.IO) {
        val connection = try {
            URL(url).openConnection() as HttpURLConnection
        } catch (e: Exception) {
            throw NetworkConnectionException("Некорректный URL или отсутствие сети", e)
        }

        try {
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000 // 5 секунд на установку соединения
            connection.readTimeout = 5000    // 5 секунд на ожидание ответа

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                throw ServerApiException("Сервер недоступен, код ответа: $responseCode")
            }
        } catch (e: java.net.SocketTimeoutException) {
            throw NetworkConnectionException("Превышено время ожидания ответа от сервера", e)
        } catch (e: java.net.UnknownHostException) {
            throw NetworkConnectionException("Хост не найден. Проверьте адрес сервера", e)
        } catch (e: Exception) {
            if (e !is ServerApiException && e !is NetworkConnectionException) {
                throw NetworkConnectionException("Ошибка сети: ${e.message}", e)
            } else {
                throw e
            }
        } finally {
            connection.disconnect()
        }
    }
}