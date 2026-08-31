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

        if (response.code == 401) {
            response.close()

            return runBlocking {
                mutex.withLock {
                    if (isRefreshing) {
                        return@withLock chain.proceed(originalRequest)
                    }

                    isRefreshing = true
                    try {
                        val refreshResponse = authApi.refreshToken()

                        if (refreshResponse.isSuccessful) {
                            chain.proceed(originalRequest)
                        } else {
                            handleAuthFailure()
                            response
                        }
                    } catch (e: Exception) {
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