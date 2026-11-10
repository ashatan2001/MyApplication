package com.example.data.network

import com.example.data.network.TokenInterceptor.Companion.REFRESH_TOKEN_PATH
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

    private companion object {
        /** Относительный путь эндпоинта обновления токена (относительно базового URL). */
        const val REFRESH_TOKEN_PATH = "auth/get-token"
    }

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
        var response = chain.proceed(request)

        if (response.code != 419 && response.code != 401) {
            return response
        }

        Timber.w("Перехвачен код ответа ${response.code} для ${request.url.encodedPath}")

        // Защита от бесконечного цикла: игнорируем ошибки самого запроса на обновление токена.
        if (isRefreshRequest(request)) {
            Timber.e("Ошибка обновления токена: бесконечный цикл предотвращён")
            response.close()
            cookieJar.clear()
            return chain.proceed(request)
        }

        // Только один поток выполняет обновление при одновременных 401/419 от нескольких запросов
        synchronized(lock) {
            Timber.d("Попытка обновления токена...")
            val refreshRequest = buildRefreshRequest(request)
            try {
                refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                    if (refreshResponse.isSuccessful) {
                        Timber.d("Токен успешно обновлён. Повтор исходного запроса.")
                        // Обязательно освобождаем ресурсы перехваченного ответа до повтора.
                        response.close()
                        response = chain.proceed(request)
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

    /** Проверяет, является ли запрос вызовом эндпоинта обновления токена. */
    private fun isRefreshRequest(request: Request): Boolean =
        request.url.encodedPath.endsWith(REFRESH_TOKEN_PATH)

    /**
     * Строит запрос на обновление токена: GET на фиксированный путь [REFRESH_TOKEN_PATH]
     * того же хоста, что и исходный бизнес-запрос. Путь и query бизнес-запроса
     * (например `/api/secure-data?x=1`) НЕ используются как URL обновления —
     * иначе запрос уходил бы в сам бизнес-эндпоинт вместо `/auth/get-token`.
     */
    private fun buildRefreshRequest(originalRequest: Request): Request {
        val refreshUrl = originalRequest.url.newBuilder()
            .encodedPath("/$REFRESH_TOKEN_PATH")
            .query(null)
            .build()

        return Request.Builder()
            .url(refreshUrl)
            .get()
            .build()
    }
}