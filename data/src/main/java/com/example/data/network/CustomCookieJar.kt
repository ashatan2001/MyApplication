package com.example.data.network

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cookieDataStore by preferencesDataStore(name = "auth_data")

@Singleton
class CustomCookieJar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) : CookieJar {

    private val cookieStore = mutableMapOf<String, List<Cookie>>()
    private val prefs = context.getSharedPreferences("auth_cookies", Context.MODE_PRIVATE)

    init {
        // Загружаем куки при инициализации
        loadFromStorage()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
        cookies.forEach { cookie ->
            Log.d("CookieJar", "Сохранена кука: ${cookie.name} = ${cookie.value.take(20)}...")
        }
        saveToStorage()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host] ?: return emptyList()
        return cookies.filter {
            it.name == "lexACCToken" || it.name == "lexRefreshToken"
        }
    }

    fun clear() {
        cookieStore.clear()
        prefs.edit().clear().apply()
        Log.d("CookieJar", "Cookie store cleared")
    }

    private fun saveToStorage() {
        val jsonStr = json.encodeToString(cookieStore.mapValues { it.value.map { cookie ->
            mapOf(
                "name" to cookie.name,
                "value" to cookie.value,
                "domain" to cookie.domain,
                "path" to cookie.path
            )
        }})
        prefs.edit().putString("cookies", jsonStr).apply()
    }

    private fun loadFromStorage() {
        val jsonStr = prefs.getString("cookies", null) ?: return
        try {
            val map = json.decodeFromString<Map<String, List<Map<String, String>>>>(jsonStr)
            cookieStore.putAll(map.mapValues { (_, cookies) ->
                cookies.map { cookieMap ->
                    Cookie.Builder()
                        .name(cookieMap["name"] ?: "")
                        .value(cookieMap["value"] ?: "")
                        .domain(cookieMap["domain"] ?: "")
                        .path(cookieMap["path"] ?: "/")
                        .build()
                }
            })
        } catch (e: Exception) {
            Log.e("CustomCookieJar", "Failed to load cookies", e)
        }
    }
}