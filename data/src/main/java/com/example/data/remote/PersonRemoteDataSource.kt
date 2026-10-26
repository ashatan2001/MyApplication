package com.example.data.remote

import com.example.data.dto.PersonDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для операций с пользователем.
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class PersonRemoteDataSource @Inject constructor(
    private val api: PersonApi
) {
    /**
     * Загружает данные пользователя по идентификатору.
     *
     * @param id Уникальный идентификатор пользователя.
     * @return [PersonDto] при успехе.
     */
    suspend fun getPersonInfo(id: Int): PersonDto = safeApiCall { api.getPerson(id) }
}