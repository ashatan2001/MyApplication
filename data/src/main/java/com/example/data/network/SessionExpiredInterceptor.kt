package com.example.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class SessionExpiredInterceptor @Inject constructor(
    private val cookieJar: CustomCookieJar,
    private val headersInterceptor: HeadersInterceptor
) : Interceptor {


    // Отдельный клиент для запроса обновления токена, чтобы не попасть в бесконечный цикл интерцепторов
    private val refreshClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(headersInterceptor)
            .build()
    }

    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 419) {
            Log.w("SessionExpiredInterceptor", "Получен код 419. Пытаемся обновить токен.")

            // Защита от бесконечного цикла, если сам запрос обновления токена вернул 419
            if (request.url.encodedPath.contains("/auth/get-token")) {
                Log.w("SessionExpiredInterceptor", "Сам запрос обновления токена вернул 419. Очищаем сессию.")
                cookieJar.clear()
                response.close()
                return response
            }

            synchronized(refreshLock) {
                val refreshUrl = request.url.newBuilder()
                    .encodedPath("/auth/get-token")
                    .build()

                val refreshRequest = Request.Builder()
                    .url(refreshUrl)
                    .get()
                    .build()

                return try {
                    val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                    if (refreshResponse.isSuccessful) {
                        Log.d("SessionExpiredInterceptor", "Токен успешно обновлен. Повторяем исходный запрос.")
                        response.close() // Обязательно закрываем старый ответ во избежание утечек (A resource failed to call release)
                        chain.proceed(request) // Повторяем исходный запрос, теперь уже с новыми куками
                    } else {
                        Log.w("SessionExpiredInterceptor", "Не удалось обновить токен (код ${refreshResponse.code}). Очищаем сессию.")
                        cookieJar.clear()
                        refreshResponse.close()
                        response // Возвращаем исходный ответ, чтобы SafeApiCall корректно показал ошибку пользователю
                    }
                } catch (e: Exception) {
                    Log.e("SessionExpiredInterceptor", "Сетевая ошибка при обновлении токена", e)
                    cookieJar.clear()
                    response
                }
            }
        }
        return response
    }
}