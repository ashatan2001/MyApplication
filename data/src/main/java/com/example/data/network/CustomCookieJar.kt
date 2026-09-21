package com.example.data.network

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomCookieJar @Inject constructor(
    private val json: Json,
    private val dataStore: DataStore<Preferences>,
    private val authEventBus: AuthEventBus
) : CookieJar {

    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cookiesKey = stringPreferencesKey("cookies")

    init {
        scope.launch {
            loadFromStorage()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existingCookies = cookieStore[host].orEmpty().toMutableList()
        val currentTime = System.currentTimeMillis()

        // Удаляем старые версии кук, которые обновляются в этом ответе
        existingCookies.removeAll { existing ->
            cookies.any { new -> new.name == existing.name && new.path == existing.path }
        }

        // Добавляем новые/обновленные куки и сразу фильтруем протухшие (Max-Age=0 и т.д.)
        val validCookies = (existingCookies + cookies).filter {
            it.expiresAt == -1L || it.expiresAt > currentTime
        }

        cookieStore[host] = validCookies

        cookies.forEach { cookie ->
            Log.d("CookieJar", "Сохранена кука: ${cookie.name} = ${cookie.value}...")
        }

        scope.launch {
            saveToStorage()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.values.flatten().filter { it.matches(url) }
    }

    fun clear() {
        cookieStore.clear()
        scope.launch {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            Log.d("CookieJar", "Cookie store cleared")
            // Уведомляем о выходе из системы
            authEventBus.notifyLogout()
        }
    }

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
            preferences[cookiesKey] = jsonStr
        }
    }

    private suspend fun loadFromStorage() {
        try {
            val preferences = dataStore.data.first()
            val jsonStr = preferences[cookiesKey] ?: return

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
            Log.d("CookieJar", "Загружено ${cookieStore.size} хостов с куками")
        } catch (e: Exception) {
            Log.e("CustomCookieJar", "Failed to load cookies", e)
        }
    }


    fun getAccessToken(): String? {
        val currentTime = System.currentTimeMillis()
        return cookieStore.values.flatten().firstOrNull {
            it.name == "lexACCToken" && (it.expiresAt == -1L || it.expiresAt > currentTime)
        }?.value
    }
}

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

