package com.example.data.remote

import com.example.data.dto.UserDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Удалённый источник данных для операций с пользователем.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class UserRemoteDataSource @Inject constructor(
    private val api: UserApi
) {
    /**
     * Загружает данные пользователя по идентификатору.
     *
     * @param id Уникальный идентификатор пользователя
     * @return [UserDto] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getUserInfo(id: Int): UserDto = safeApiCall { api.getUser(id) }
}