package com.example.data.network

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TokenInterceptorTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var mockCookieJar: CustomCookieJar
    private lateinit var mockDynamicInterceptor: DynamicBaseUrlInterceptor

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        mockCookieJar = mockk(relaxed = true)
        mockDynamicInterceptor = mockk()

        // Настраиваем мок интерсептора просто пропускать запросы без изменений
        every { mockDynamicInterceptor.intercept(any()) } answers {
            val chain = firstArg<Interceptor.Chain>()
            chain.proceed(chain.request())
        }

        val interceptor = TokenInterceptor(
            cookieJar = mockCookieJar,
            dynamicBaseUrlInterceptor = mockDynamicInterceptor
        )

        client = OkHttpClient.Builder()
            .cookieJar(mockCookieJar)
            .addInterceptor(interceptor)
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        clearMocks(mockCookieJar, mockDynamicInterceptor)
    }

    @Test
    fun `при ошибке 419 должен обновить токен и повторить запрос`() {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(419))
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Set-Cookie", "lexACCToken=new_valid_token; Path=/")
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Success Data"))

        // When
        val request = Request.Builder().url(mockWebServer.url("/api/secure-data")).build()
        val response = client.newCall(request).execute()

        // Then
        assertEquals(200, response.code)
        assertEquals("Success Data", response.body?.string())
        assertEquals(3, mockWebServer.requestCount)

        val request1 = mockWebServer.takeRequest()
        assertEquals("/api/secure-data", request1.path)

        val request2 = mockWebServer.takeRequest()
        assertEquals("/auth/get-token", request2.path)

        val request3 = mockWebServer.takeRequest()
        assertEquals("/api/secure-data", request3.path)

        verify(exactly = 0) { mockCookieJar.clear() }
    }

    @Test
    fun `при неудачном обновлении токена должен очистить куки и вернуть исходную ошибку`() {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(419))
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // When
        val request = Request.Builder().url(mockWebServer.url("/api/secure-data")).build()
        val response = client.newCall(request).execute()

        // Then
        assertEquals(419, response.code)
        assertEquals(2, mockWebServer.requestCount)
        verify(exactly = 1) { mockCookieJar.clear() }
    }

    @Test
    fun `при успешном ответе не должен пытаться обновлять токен`() {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))

        // When
        val request = Request.Builder().url(mockWebServer.url("/api/data")).build()
        val response = client.newCall(request).execute()

        // Then
        assertEquals(200, response.code)
        assertEquals("OK", response.body?.string())
        assertEquals(1, mockWebServer.requestCount)
        verify(exactly = 0) { mockCookieJar.clear() }
    }
}