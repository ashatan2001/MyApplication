package com.example.data.remote

import com.example.data.dto.UserDto
import com.example.data.util.safeApiCall
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API для операций пользователем.
 *
 * Все методы возвращают [Response], чтобы [safeApiCall]
 * мог получить доступ к коду ответа и телу ошибки для детального маппинга исключений.
 */
interface UserApi {

    /**
     * Получение данных пользователя по идентификатору.
     *
     * @param id Уникальный идентификатор пользователя (извлекается из токена).
     * @return [UserDto] с данными профиля.
     *
     * Возможные коды ошибок:
     * - 401/403 — сессия недействительна
     * - 404 — пользователь не найден
     * - 419 — сессия истекла (обрабатывается [com.example.data.network.TokenInterceptor])
     * - 5xx — ошибка сервера
     */
    @GET("api/persons/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<UserDto>
}