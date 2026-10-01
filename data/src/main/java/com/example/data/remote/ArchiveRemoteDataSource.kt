package com.example.data.remote

import com.example.data.dto.ArchiveApi
import com.example.data.dto.DstPointDto
import com.example.data.dto.EventDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Удалённый источник данных для операций c точками выгруза бетона.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class ArchiveRemoteDataSource @Inject constructor(
    private val api: ArchiveApi
) {
    /**
     * Загружает архив событий
     *
     * @return [List<EventDto>] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getEvents(): List<EventDto> = safeApiCall { api.getEvents() }
}