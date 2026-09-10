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
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val localDataSource: AuthLocalDataSource,
    private val cookieJar: CustomCookieJar,
    private val authApiProvider: Provider<AuthApi>
) : Interceptor {

    private val mutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code != 401) return response

        // ⚠️ ИСПРАВЛЕНИЕ ДЕДЛОКА:
        // Если 401 пришел на эндпоинты логина или рефреша,
        // не пытаемся рефрешить токен. Просто возвращаем ответ в SafeApiCall.
        val path = request.url.encodedPath
        if (path.contains("auth/sign-in") || path.contains("auth/get-token")) {
            return response
        }

        response.close()

        return runBlocking(Dispatchers.IO) {
            mutex.withLock {
                try {
                    val authApi = authApiProvider.get()
                    val refreshResponse = authApi.refreshToken()

                    if (refreshResponse.isSuccessful) {
                        // Повторяем исходный запрос с обновленными cookies
                        chain.proceed(request)
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
    }

    private fun createErrorResponse(
        request: okhttp3.Request,
        code: Int,
        message: String,
    ): Response =
        Response.Builder()
            .code(code)
            .message(message)
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .body("".toResponseBody(null))
            .build()
}