package com.example.data.network

import com.example.data.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Интерсептор для добавления общих заголовков ко всем исходящим запросам.
 *
 * Устанавливает заголовок `Accept` с версией API,
 * чтобы сервер возвращал данные в ожидаемом формате.
 */
@Singleton
class HeadersInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .header("Accept", "application/json; version=${BuildConfig.API_VERSION}")
            .build()
        return chain.proceed(newRequest)
    }
}