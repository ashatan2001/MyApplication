package com.example.data.remote

import com.example.data.dto.LoginRequestDto
import com.example.data.dto.LoginResponseDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для операций аутентификации.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi
) {
    /**
     * Выполняет запрос на вход в систему.
     *
     * @param login Логин пользователя.
     * @param password Пароль пользователя.
     * @return [LoginResponseDto] при успехе.
     */
    suspend fun login(login: String, password: String): LoginResponseDto {
        val requestDto = LoginRequestDto(username = login, password = password)
        return safeApiCall { api.login(requestDto) }
    }

    /** Обновляет токен сессии. Вызывается из [TokenInterceptor]. */
    suspend fun refreshToken() {
        safeApiCall<Unit> { api.refreshToken() }
    }

    /**
     * Инвалидирует сессию на сервере.
     * При сетевой ошибке исключение пробрасывается в репозиторий,
     * который всё равно выполняет локальную очистку.
     */
    suspend fun logout() {
        safeApiCall<Unit> { api.logout() }
    }
}