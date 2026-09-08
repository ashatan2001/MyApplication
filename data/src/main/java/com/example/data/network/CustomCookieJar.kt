package com.example.data.network

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.data.local.AuthLocalDataSource
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
class CustomCookieJar
    @Inject
    constructor() : CookieJar {
        // Храним куки в памяти для быстрого доступа
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(
            url: HttpUrl,
            cookies: List<Cookie>,
        ) {
            // Сохраняем все куки, пришедшие от сервера
            cookieStore[url.host] = cookies

            // Для отладки — смотрим, что пришло
            cookies.forEach { cookie ->
                Log.d("CookieJar", "Сохранена кука: ${cookie.name} = ${cookie.value.take(20)}...")
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookies = cookieStore[url.host] ?: return emptyList()

            // Фильтруем только нужные куки (опционально, можно отдавать все)
            return cookies.filter {
                it.name == "lexACCToken" || it.name == "lexRefreshToken"
            }
        }

        fun clear() {
            cookieStore.clear()
        }
    }
