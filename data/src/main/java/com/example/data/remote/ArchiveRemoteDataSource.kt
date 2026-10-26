package com.example.data.remote

import com.example.data.dto.ArchiveDto
import com.example.data.dto.EventDto
import com.example.data.dto.HandLoadDto
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
     */
    suspend fun getEventArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int
    ): ArchiveDto<EventDto> = safeApiCall(
        emptyValue = ArchiveDto(
            startDate = "", endDate = "", pageNum = 0, pages = 0, records = 0, data = emptyList()
        )
    ) {
        api.getEvents(startDate, endDate, pageNum, records)
    }

    suspend fun getHandLoadArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int
    ): ArchiveDto<HandLoadDto> = safeApiCall(
        emptyValue = ArchiveDto(
            startDate = "", endDate = "", pageNum = 0, pages = 0, records = 0, data = emptyList()
        )
    ) {
        api.getHandLoads(startDate, endDate, pageNum, records)
    }
}