package com.example.data.remote

import com.example.data.dto.LoginRequestDto
import com.example.data.dto.LoginResponseDto
import com.example.data.util.safeApiCall
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API для операций аутентификации.
 *
 * Все методы возвращают [Response], чтобы [safeApiCall]
 * мог получить доступ к коду ответа и телу ошибки для детального маппинга исключений.
 */
interface AuthApi {
    /**
     * Авторизация пользователя.
     *
     * @param body Учётные данные (логин и пароль).
     * @return [LoginResponseDto] с именем пользователя и приветствием.
     */
    @POST("auth/sign-in")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    /**
     * Обновление токена сессии.
     *
     * Вызывается из [com.example.data.network.TokenInterceptor] при получении 401/419.
     * Возвращает [Unit], так как сервер не передаёт тело при успехе (204).
     */
    @GET("auth/get-token")
    suspend fun refreshToken(): Response<Unit>

    /**
     * Выход из системы (инвалидация сессии на сервере).
     * Возвращает [Unit], так как тело ответа не требуется.
     */
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}