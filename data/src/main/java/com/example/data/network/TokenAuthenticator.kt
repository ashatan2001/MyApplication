package com.example.data.network

import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenAuthenticator @Inject constructor(
    private val cookieJar: CustomCookieJar
) : Authenticator {
    private val refreshClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401 && response.code != 419) return null

        if (response.request.url.encodedPath.contains("/auth/get-token")) {
            cookieJar.clear()
            return null
        }

        val originalUrl = response.request.url
        val refreshUrl = "${originalUrl.scheme}://${originalUrl.host}:${originalUrl.port}/auth/get-token"

        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .get()
            .build()

        return try {
            val refreshResponse = refreshClient.newCall(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {
                response.request.newBuilder().build()
            } else {
                cookieJar.clear()
                null
            }
        } catch (e: IOException) {
            null
        }
    }
}