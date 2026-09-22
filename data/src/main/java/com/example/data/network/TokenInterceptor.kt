package com.example.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenInterceptor @Inject constructor(
    private val cookieJar: CustomCookieJar
) : Interceptor {

    private val lock = Any() // Блокировка для предотвращения параллельного обновления

    // Клиент ТОЛЬКО для обновления. БЕЗ этого интерсептора, чтобы избежать рекурсии.
    private val refreshClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Перехватываем 419 (и 401 на всякий случай)
        if (response.code != 419 && response.code != 401) {
            return response
        }

        // Защита от бесконечного цикла, если сам refresh-запрос вернул ошибку
        if (request.url.encodedPath.contains("/auth/get-token")) {
            cookieJar.clear()
            return response
        }

        synchronized(lock) {
            val refreshRequest = Request.Builder()
                .url(request.url.newBuilder().encodedPath("/auth/get-token").build())
                .get()
                .build()

            try {
                val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                if (refreshResponse.isSuccessful) {
                    refreshResponse.close()
                    response.close() // Закрываем старый ответ с 419
                    // Повторяем исходный запрос (cookieJar уже подхватит новые куки)
                    return chain.proceed(request)
                } else {
                    refreshResponse.close()
                    cookieJar.clear() // Refresh не удался -> выходим
                }
            } catch (e: Exception) {
                cookieJar.clear()
            }
        }

        return response
    }
}