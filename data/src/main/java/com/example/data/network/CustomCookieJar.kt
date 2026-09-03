package com.example.data.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
        by androidx.datastore.preferences.preferencesDataStore(name = "auth_cookies_store")

@Singleton
class CustomCookieJar @Inject constructor(
    @ApplicationContext private val context: Context
) : CookieJar {
    private val cookieCache = mutableMapOf<String, MutableSet<String>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val cookieStrings = cookies.map { it.toString() }.toMutableSet()
        synchronized(cookieCache) { cookieCache[host] = cookieStrings }
        runBlocking {
            context.authDataStore.edit {
                it[stringSetPreferencesKey(host)] = cookieStrings
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        synchronized(cookieCache) {
            cookieCache[host]?.let { return parseCookies(url, it) }
        }
        val cookieStrings = runBlocking {
            context.authDataStore.data.first()[stringSetPreferencesKey(host)] ?: emptySet()
        }
        synchronized(cookieCache) { cookieCache[host] = cookieStrings.toMutableSet() }
        return parseCookies(url, cookieStrings)
    }

    fun clear() {
        synchronized(cookieCache) { cookieCache.clear() }
        runBlocking { context.authDataStore.edit { it.clear() } }
    }

    private fun parseCookies(url: HttpUrl, strings: Set<String>): List<Cookie> =
        strings.mapNotNull { Cookie.parse(url, it) }
}