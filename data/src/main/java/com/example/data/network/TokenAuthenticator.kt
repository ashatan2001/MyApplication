package com.example.data.network

import android.util.Log
import okhttp3.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenAuthenticator @Inject constructor(
    private val cookieJar: CustomCookieJar,
    private val headersInterceptor: HeadersInterceptor
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    private val refreshClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(headersInterceptor)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val originalUrl = response.request.url
        val responseCode = response.code

        Log.d(TAG, "Обнаружен код $responseCode для запроса: $originalUrl")

        if (responseCode != 401 && responseCode != 419) {
            Log.d(TAG, "Игнорируем: код $responseCode не требует обновления токена")
            return null
        }

        // Защита от бесконечного цикла при попытке обновить сам refresh-токен
        if (originalUrl.encodedPath.contains("/auth/get-token")) {
            Log.w(TAG, "Сам запрос обновления токена вернул ошибку. Очищаем сессию.")
            cookieJar.clear()
            return null
        }

        if (response.priorResponse != null) {
            Log.w(TAG, "Повторный 401/419 после refresh. Очищаем сессию.")
            cookieJar.clear()
            return null
        }

        val refreshUrl = originalUrl.newBuilder()
            .encodedPath("/auth/get-token")
            .build()

        Log.d(TAG, "Пытаемся обновить токен по адресу: $refreshUrl")

        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .get()
            .build()

        return try {
            val refreshResponse = refreshClient.newCall(refreshRequest).execute()
            val refreshCode = refreshResponse.code

            Log.d(TAG, "Ответ от сервера обновления токена: $refreshCode")

            if (refreshResponse.isSuccessful) {
                Log.d(TAG, "Токен успешно обновлен. Повторяем исходный запрос.")
                // Важно: при успешном ответе CookieJar уже сохранил новые куки из ответа
                response.request.newBuilder().build()
            } else {
                Log.w(TAG, "Не удалось обновить токен (код $refreshCode). Выход из системы.")
                cookieJar.clear()
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Сетевая ошибка при обновлении токена", e)
            cookieJar.clear()
            null
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка при обновлении токена", e)
            cookieJar.clear()
            null
        }
    }
}