package com.example.data.remote

import com.example.data.dto.ArchiveApi
import com.example.data.dto.ArchiveDto
import com.example.data.dto.EventDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для работы с архивами.
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
     * @return [ArchiveDto<EventDto>] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getEventArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        pages: Int,
        records: Int):
            ArchiveDto<EventDto> = safeApiCall { api.getEvents() }
}