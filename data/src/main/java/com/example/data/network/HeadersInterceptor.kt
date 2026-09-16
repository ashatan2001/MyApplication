package com.example.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.example.data.BuildConfig

@Singleton
class HeadersInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .header("Accept", "application/json; version=${BuildConfig.API_VERSION}")
            .build()
        return chain.proceed(newRequest)
    }
}