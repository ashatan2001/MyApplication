package com.example.data.network

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class TokenAuthenticatorTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var cookieJar: CustomCookieJar
    private lateinit var authenticator: TokenAuthenticator
    private lateinit var headersInterceptor: HeadersInterceptor

    @Before
    fun setup() {
        mockServer = MockWebServer().apply {
            start(3080) // Порт как в вашем приложении
        }

        val mockContext = mockk<Context>(relaxed = true)
        cookieJar = CustomCookieJar(mockContext, Json { ignoreUnknownKeys = true })
        headersInterceptor = HeadersInterceptor()
        authenticator = TokenAuthenticator(cookieJar, headersInterceptor)
    }

    @After
    fun teardown() {
        mockServer.shutdown()
    }

    @Test
    fun `should refresh token on 419 and retry request`() = runTest {
        // Arrange: готовим очередь ответов
        mockServer.enqueue(MockResponse().setResponseCode(419))
        mockServer.enqueue(MockResponse()
            .addHeader("Set-Cookie", "lexACCToken=newtoken; Path=/")
            .setResponseCode(200)
            .setBody("{}"))
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"PersonID":"2","FIO":"Test"}"""))

        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .authenticator(authenticator)
            .addInterceptor(headersInterceptor)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(mockServer.url("/api/persons/2"))
            .build()

        // Act
        val response = client.newCall(request).execute()

        // Assert
        assert(response.isSuccessful)
        assert(response.code == 200)
        assert(mockServer.requestCount == 3) // Первый запрос + refresh + retry
    }
}