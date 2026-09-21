package com.example.data.network

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        // Запускаем MockWebServer (порт будет выбран автоматически)
        mockServer = MockWebServer().apply { start() }

        // Мокаем Android Context и Log, чтобы избежать ошибок в JVM-тестах
        val mockContext = mockk<Context>(relaxed = true)
        mockkStatic("android.util.Log")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0

        cookieJar = mockk(relaxed = true)

        headersInterceptor = HeadersInterceptor()
        authenticator = TokenAuthenticator(cookieJar, headersInterceptor)
    }

    @After
    fun teardown() {
        mockServer.shutdown()
    }

    // TokenAuthenticatorTest.kt
    @Test
    fun `should refresh token on 419 (mapped to 401) and retry request`() = runTest {
        // Arrange
        mockServer.enqueue(MockResponse().setResponseCode(419))
        mockServer.enqueue(MockResponse().setResponseCode(200).addHeader("Set-Cookie", "lexACCToken=new_valid_token; Path=/"))
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"PersonID":"2","FIO":"Test User"}"""))

        // Создаем интерцептор без зависимостей
        val sessionInterceptor = SessionExpiredInterceptor()

        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(sessionInterceptor)   // <-- ДО authenticator
            .addInterceptor(headersInterceptor)
            .authenticator(authenticator)         // <-- ПОСЛЕ interceptor'ов
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(mockServer.url("/api/persons/2")).build()

        // Act
        val response = client.newCall(request).execute()

        // Assert
        assertTrue("Ожидался 200, получен: ${response.code}", response.isSuccessful)
        assertEquals(3, mockServer.requestCount)

        mockServer.takeRequest() // Исходный (419 → 401)
        val tokenRequest = mockServer.takeRequest() // Refresh
        assertTrue(tokenRequest.path?.contains("/auth/get-token") == true)
    }
}