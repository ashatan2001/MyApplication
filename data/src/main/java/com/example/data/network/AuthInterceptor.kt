package com.example.data.network

import android.util.Log
import com.example.data.local.AuthLocalDataSource
import com.example.data.remote.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Provider // 1. Добавьте этот импорт
import javax.inject.Singleton

@Singleton
class AuthInterceptor
@Inject
constructor(
    private val localDataSource: AuthLocalDataSource,
    private val cookieJar: CustomCookieJar,
    private val authApiProvider: Provider<AuthApi>,
    private val authEventBus: AuthEventBus
) : Interceptor {
    private val mutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code != 401) return response
        response.close()

        // Используем синхронный вызов, так как Interceptor требует синхронного ответа
        return runBlocking {
            mutex.withLock {
                try {
                    val authApi = authApiProvider.get()
                    val refreshResponse = authApi.refreshToken()

                    if (refreshResponse.isSuccessful) {
                        // Повторяем оригинальный запрос с обновленными куки
                        val newRequest = chain.request().newBuilder().build()
                        chain.proceed(newRequest)
                    } else {
                        handleAuthFailure()
                        createErrorResponse(request, 401, "Unauthorized")
                    }
                } catch (e: Exception) {
                    Log.e("AuthInterceptor", "Refresh failed", e)
                    handleAuthFailure()
                    createErrorResponse(request, 401, "Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun handleAuthFailure() {
        cookieJar.clear()
        localDataSource.clearSession()
        authEventBus.notifyLogout()
        Log.w("AuthInterceptor", "Auth failed - session cleared")
    }

    private fun createErrorResponse(
        request: okhttp3.Request,
        code: Int,
        message: String,
    ): Response =
        Response
            .Builder()
            .code(code)
            .message(message)
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .body("".toResponseBody(null))
            .build()
}
