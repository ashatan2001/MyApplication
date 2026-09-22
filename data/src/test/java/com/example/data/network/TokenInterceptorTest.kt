package com.example.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock


class TokenInterceptorTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var mockCookieJar: CustomCookieJar

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockCookieJar = mock()

        val interceptor = TokenInterceptor(mockCookieJar)
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `при ошибке 419 должен обновить токен и повторить запрос`() {
        // 1. Сценарий ответов сервера
        // Запрос 1: Исходный запрос падает с 419
        mockWebServer.enqueue(MockResponse().setResponseCode(419))
        // Запрос 2: Запрос на обновление токена успешен и возвращает новую куку
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Set-Cookie", "lexACCToken=new_valid_token; Path=/")
        )
        // Запрос 3: Повтор исходного запроса теперь успешен
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Success Data"))

        // 2. Выполняем запрос
        val request = Request.Builder().url(mockWebServer.url("/api/secure-data")).build()
        val response = client.newCall(request).execute()

        // 3. Проверки
        assertEquals(200, response.code)
        assertEquals("Success Data", response.body?.string())

        // Проверяем, что было сделано ровно 3 запроса
        assertEquals(3, mockWebServer.requestCount)

        // Проверяем, что при неудачном обновлении вызывается clear()
        // (Этот тест проверяет успешный сценарий, clear вызван не будет)
    }
}