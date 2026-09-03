package com.example.data.network

import android.util.Log
import com.example.data.remote.AuthApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val cookieJar: CustomCookieJar,
    private val authEventBus: AuthEventBus,
    private val authApi: AuthApi
) : Interceptor {
    private val mutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        if (response.code != 401) return response

        response.close()
        return runBlocking {
            mutex.withLock {
                try {
                    val refreshResponse = authApi.refreshToken()
                    if (refreshResponse.isSuccessful) {
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
        try {
            authEventBus.notifyLogout()
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error notifying logout", e)
        }
    }

    private fun createErrorResponse(
        request: okhttp3.Request,
        code: Int,
        message: String
    ): Response = Response.Builder()
        .code(code)
        .message(message)
        .request(request)
        .protocol(okhttp3.Protocol.HTTP_1_1)
        .body("".toResponseBody(null))
        .build()
}