package com.example.data.network

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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

    @Test
    fun `should refresh token on 401 and retry request`() = runTest {
        // 1. Arrange: Готовим очередь ответов от сервера
        // Ответ 1: Исходный запрос получает 419
        mockServer.enqueue(MockResponse().setResponseCode(401))

        // Ответ 2: Запрос на обновление токена успешен, сервер возвращает новую куку
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Set-Cookie", "lexACCToken=new_valid_token; Path=/")
                .setBody("""{"status":"ok"}""")
        )

        // Ответ 3: Повторный исходный запрос теперь успешен
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"PersonID":"2","FIO":"Test User"}""")
        )

        // 2. Собираем клиент с нашим аутентификатором
        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .authenticator(authenticator)
            .addInterceptor(headersInterceptor)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(mockServer.url("/api/persons/2"))
            .build()

        // 3. Act: Выполняем запрос
        val response = client.newCall(request).execute()

        // 4. Assert: Проверяем результаты с понятными сообщениями об ошибках

        // Если тест упадет здесь, мы увидим реальный код ответа в консоли
        assertTrue("Ожидался успешный ответ (200), но получен код: ${response.code}. Body: ${response.body?.string()}", response.isSuccessful)
        assertEquals(200, response.code)

        // Проверяем, что было сделано ровно 3 запроса:
        // 1. Исходный (419) -> 2. Обновление токена (200) -> 3. Повтор исходного (200)
        assertEquals("Должно быть выполнено ровно 3 запроса", 3, mockServer.requestCount)

        // Дополнительно проверим, что второй запрос был именно на эндпоинт обновления
        val refreshRequest = mockServer.takeRequest() // Забираем 1-й запрос (419)
        val tokenRequest = mockServer.takeRequest()   // Забираем 2-й запрос (обновление)
        assertTrue("Второй запрос должен быть на /auth/get-token", tokenRequest.path?.contains("/auth/get-token") == true)
    }
}