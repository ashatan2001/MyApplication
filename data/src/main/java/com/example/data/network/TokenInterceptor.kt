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
 * Перехватывает ответы с кодами 401 (Unauthorized) и 419 (Session Expired).
 * При получении такого кода выполняет синхронный запрос на обновление токена.
 * В случае успеха повторяет исходный запрос с обновлёнными куки.
 *
 * @param cookieJar Реализация [CustomCookieJar] для управления хранением и очисткой кук.
 * @param dynamicBaseUrlInterceptor Интерсептор динамической подмены URL,
 *        добавляемый в [refreshClient] для корректной работы при смене сервера.
 */
@Singleton
class TokenInterceptor @Inject constructor(
    private val cookieJar: CustomCookieJar,
    private val dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
) : Interceptor {

    /** Блокировка для предотвращения race condition при параллельных попытках обновления токена. */
    private val lock = Any()

    /**
     * Отдельный клиент OkHttp для выполнения запроса на обновление токена.
     * Инициализируется лениво (lazy) БЕЗ [TokenInterceptor], чтобы избежать бесконечной рекурсии.
     *
     * **Важно:** включает [DynamicBaseUrlInterceptor], чтобы запрос на `/auth/get-token`
     * шёл на актуальный (возможно, изменённый пользователем) адрес сервера.
     */
    private val refreshClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(dynamicBaseUrlInterceptor)
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
        if (request.url.encodedPath.endsWith("/auth/get-token")) {
            Timber.e("Ошибка обновления токена: бесконечный цикл предотвращён")
            response.close()
            cookieJar.clear()
            return response
        }

        // Только один поток выполняет обновление при одновременных 401 от нескольких запросов
        synchronized(lock) {
            Timber.d("Попытка обновления токена...")
            val refreshRequest = Request.Builder()
                .url(request.url.newBuilder().encodedQuery(null).build())
                .get()
                .build()
            try {
                refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                    if (refreshResponse.isSuccessful) {
                        Timber.d("Токен успешно обновлён. Повтор исходного запроса.")
                        response.close()
                        return chain.proceed(request)
                    } else {
                        Timber.w("Не удалось обновить токен. Код ответа: ${refreshResponse.code}")
                        cookieJar.clear()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Исключение при попытке обновления токена")
                cookieJar.clear()
            }
        }
        return response
    }
}