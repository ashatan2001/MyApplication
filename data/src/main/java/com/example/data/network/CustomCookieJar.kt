package com.example.data.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.local.AuthLocalDataSource
import com.example.domain.event.AuthEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация [CookieJar] с персистентным хранением кук аутентификации.
 *
 * Архитектура:
 * - [cookieStore] (ConcurrentHashMap) — быстрый доступ в памяти для синхронных запросов.
 * - [DataStore] — персистентное хранение для восстановления сессии после перезапуска приложения.
 *
 * Куки автоматически фильтруются по сроку действия при сохранении и загрузке.
 * При очистке ([clear]) удаляются как куки, так и сессия пользователя,
 * после чего отправляется событие выхода через [AuthEventBus].
 */
@Singleton
class CustomCookieJar @Inject constructor(
    private val json: Json,
    private val dataStore: DataStore<Preferences>,
    private val authEventBus: AuthEventBus,
    private val authLocalDataSource: AuthLocalDataSource
) : CookieJar {

    private companion object {
        /** Имя куки, содержащей access-токен. */
        const val ACCESS_TOKEN_COOKIE_NAME = "lexACCToken"

        /** Ключ для хранения сериализованных кук в DataStore. */
        val COOKIES_KEY = stringPreferencesKey("cookies")
    }

    /** Потокобезопасное хранилище кук в памяти. Ключ — хост. */
    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()

    /** Scope для фоновых операций с DataStore. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Флаг завершения начальной загрузки кук из хранилища. */
    private val isLoaded = java.util.concurrent.atomic.AtomicBoolean(false)

    init {
        scope.launch {
            loadFromStorage()
            isLoaded.set(true)
        }
    }

    /**
     * Вызывается OkHttp при получении ответа от сервера.
     * Обновляет куки для хоста, удаляя старые версии и протухшие куки.
     */
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existingCookies = cookieStore[host].orEmpty().toMutableList()
        val currentTime = System.currentTimeMillis()

        // Удаляем старые версии кук, которые обновляются в этом ответе
        existingCookies.removeAll { existing ->
            cookies.any { new -> new.name == existing.name && new.path == existing.path }
        }

        // Добавляем новые/обновлённые куки и сразу фильтруем протухшие (Max-Age=0 и т.д.)
        val validCookies = (existingCookies + cookies).filter {
            it.expiresAt == -1L || it.expiresAt > currentTime
        }

        cookieStore[host] = validCookies

        cookies.forEach { cookie ->
            Timber.d("Сохранена кука: ${cookie.name}")
        }

        scope.launch {
            saveToStorage()
        }
    }

    /**
     * Вызывается OkHttp перед отправкой запроса.
     * Возвращает все куки, подходящие для указанного URL.
     */
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.values.flatten().filter { it.matches(url) }
    }

    /**
     * Полностью очищает куки, сессию и оповещает приложение о выходе.
     * Вызывается при неудачном обновлении токена или выходе пользователя.
     */
    fun clear() {
        cookieStore.clear()

        scope.launch {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            authLocalDataSource.clearSession()
            Timber.d("Cookie store and auth session fully cleared")
            authEventBus.notifyLogout()
        }
    }

    /**
     * Возвращает текущий access-токен из кук.
     *
     * @return Значение токена или null, если кука не найдена или истекла.
     */
    fun getAccessToken(): String? {
        val currentTime = System.currentTimeMillis()
        return cookieStore.values.flatten().firstOrNull {
            it.name == ACCESS_TOKEN_COOKIE_NAME && (it.expiresAt == -1L || it.expiresAt > currentTime)
        }?.value
    }

    /** Сериализует куки и сохраняет в DataStore. */
    private suspend fun saveToStorage() {
        val jsonStr = json.encodeToString(cookieStore.mapValues { (_, cookies) ->
            cookies.map { cookie ->
                CookieData(
                    name = cookie.name,
                    value = cookie.value,
                    domain = cookie.domain,
                    path = cookie.path,
                    expiresAt = cookie.expiresAt,
                    secure = cookie.secure,
                    httpOnly = cookie.httpOnly,
                    hostOnly = cookie.hostOnly
                )
            }
        })

        dataStore.edit { preferences ->
            preferences[COOKIES_KEY] = jsonStr
        }
    }

    /** Загружает куки из DataStore при старте приложения. */
    private suspend fun loadFromStorage() {
        try {
            val preferences = dataStore.data.first()
            val jsonStr = preferences[COOKIES_KEY] ?: return

            val map = json.decodeFromString<Map<String, List<CookieData>>>(jsonStr)

            cookieStore.putAll(map.mapValues { (_, cookies) ->
                cookies.map { cookieData ->
                    Cookie.Builder()
                        .name(cookieData.name)
                        .value(cookieData.value)
                        .domain(cookieData.domain)
                        .path(cookieData.path)
                        .expiresAt(cookieData.expiresAt)
                        .apply {
                            if (cookieData.secure) secure()
                            if (cookieData.httpOnly) httpOnly()
                            if (cookieData.hostOnly) hostOnlyDomain(cookieData.domain)
                        }
                        .build()
                }
            })

            Timber.d("Загружено ${cookieStore.size} хостов с куками")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load cookies from storage")
        }
    }
}

/**
 * Сериализуемое представление куки для хранения в DataStore.
 * [Cookie] не реализует Serializable, поэтому используется этот DTO.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class CookieData(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean
)