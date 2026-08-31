package com.example.data.network

import com.example.data.remote.AuthApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val cookieJar: CustomCookieJar,
    private val authEventBus: AuthEventBus,
    private val authApi: AuthApi
) : Interceptor {

    // Mutex гарантирует, что только один поток может выполнять блок кода внутри withLock
    private val mutex = Mutex()
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        // Если сервер вернул 401 Unauthorized
        if (response.code == 401) {
            // ВАЖНО: Закрываем старый ответ, чтобы избежать утечки ресурсов (ConnectionLeak)
            response.close()

            // runBlocking блокирует текущий поток OkHttp до завершения корутины
            return runBlocking {
                // withLock гарантирует очередь: если один поток обновляет токен,
                // остальные ждут его завершения и не вызывают refreshToken повторно
                mutex.withLock {
                    if (isRefreshing) {
                        // Другой поток уже обновил токен.
                        // Просто повторяем запрос: CustomCookieJar уже подставит новые куки.
                        return@withLock chain.proceed(originalRequest)
                    }

                    isRefreshing = true
                    try {
                        // Вызываем suspend функцию внутри runBlocking
                        val refreshResponse = authApi.refreshToken()

                        if (refreshResponse.isSuccessful) {
                            // Токен обновлен, куки сохранены через CustomCookieJar.
                            // Повторяем исходный запрос.
                            chain.proceed(originalRequest)
                        } else {
                            // Refresh не удался (например, refresh токен тоже истек)
                            handleAuthFailure()
                            response // Возвращаем оригинальный 401 ответ
                        }
                    } catch (e: Exception) {
                        // Ошибка сети или другая ошибка при refresh
                        handleAuthFailure()
                        response
                    } finally {
                        isRefreshing = false
                    }
                }
            }
        }

        return response
    }

    private fun handleAuthFailure() {
        cookieJar.clear()
        authEventBus.notifyLogout()
    }
}