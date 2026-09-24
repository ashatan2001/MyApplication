package com.example.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Перехватчик HTTP-запросов для автоматического обновления токена аутентификации.
 *
 * Перехватывает ответы с кодами 401 (Unauthorized) и 419 (Session Expired).
 * При получении такого кода выполняет синхронный запрос на обновление токена.
 * В случае успеха повторяет исходный запрос с обновленными куки.
 *
 * @param cookieJar Реализация [CustomCookieJar] для управления хранением и очисткой кук.
 */
@Singleton
class TokenInterceptor @Inject constructor(
    private val cookieJar: CustomCookieJar
) : Interceptor {

    // Блокировка для предотвращения race condition при параллельных попытках обновления токена
    private val lock = Any()

    /**
     * Отдельный клиент OkHttp для выполнения запроса на обновление токена.
     * Инициализируется лениво (lazy) БЕЗ этого интерсептора, чтобы избежать бесконечной рекурсии.
     */
    private val refreshClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code != 419 && response.code != 401) {
            return response
        }

        Timber.w("Перехвачен код ответа ${response.code} для ${request.url.encodedPath}")

        // Защита от бесконечного цикла: игнорируем ошибки самого запроса на обновление токена.
        // Используем endsWith для точности, чтобы не задеть другие пути, содержащие эту подстроку.
        if (request.url.encodedPath.endsWith("/auth/get-token")) {
            Timber.e("Ошибка обновления токена: бесконечный цикл предотвращен")
            response.close()
            cookieJar.clear()
            return response
        }

        // Гарантируем, что только один поток выполнит обновление
        synchronized(lock) {
            Timber.d("Попытка обновления токена...")
            val refreshRequest = Request.Builder()
                .url(request.url.newBuilder().encodedPath("/auth/get-token").build())
                .get()
                .build()

            try {
                refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                    if (refreshResponse.isSuccessful) {
                        Timber.d("Токен успешно обновлен. Повтор исходного запроса.")
                        response.close() // Закрываем исходный ответ с ошибкой 419/401
                        return chain.proceed(request) // Повтор с обновленным CookieJar
                    } else {
                        Timber.w("Не удалось обновить токен. Код ответа: ${refreshResponse.code}")
                        cookieJar.clear() // Refresh не удался (токены протухли полностью)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Исключение при попытке обновления токена")
                cookieJar.clear()  // Очистка при сетевой ошибке во время обновления
            }
        }

        return response
    }
}