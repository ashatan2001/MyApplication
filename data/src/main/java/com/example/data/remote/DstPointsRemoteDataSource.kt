package com.example.data.remote

import com.example.data.dto.DstPointDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для операций c точками выгрузки бетона.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class DstPointsRemoteDataSource @Inject constructor(
    private val api: DstPointsApi
) {
    /**
     * Загружает данные точки выгрузки бетона по ее коду.
     *
     * @param zoneId код точки выгрузки бетона
     * @return [DstPointDto] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getDstPoint(zoneId: Int): DstPointDto = safeApiCall { api.getDstPoint(zoneId) }

    /**
     * Загружает список точек выгрузки бетона.
     *
     * @return [List<DstPointDto>] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getDstPointsList(): List<DstPointDto> = safeApiCall { api.getDstPointsList() }
}