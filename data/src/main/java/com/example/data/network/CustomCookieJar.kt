package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomCookieJar @Inject constructor (
    @ApplicationContext private val context: Context
): CookieJar {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_cookies", Context.MODE_PRIVATE)

    private val cookieCache = mutableMapOf<String, MutableSet<String>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val cookieStrings = cookies.map { it.toString() }.toMutableSet()

        synchronized(this) {
            cookieCache[host] = cookieStrings
        }

        prefs.edit().putStringSet(host, cookieStrings).apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host

        synchronized(this) {
            if (cookieCache.containsKey(host)) {
                return parseCookies(url, cookieCache[host]!!)
            }
        }

        val cookieStrings = prefs.getStringSet(host, emptySet()) ?: emptySet()

        synchronized(this) {
            cookieCache[host] = cookieStrings.toMutableSet()
        }

        return parseCookies(url, cookieStrings)
    }

    fun clear() {
        synchronized(this) {
            cookieCache.clear()
        }
        prefs.edit().clear().apply()
    }

    private fun parseCookies(url: HttpUrl, strings: Set<String>): List<Cookie> {
        return strings.mapNotNull { Cookie.parse(url, it) }
    }
}