package com.example.data.remote

import com.example.data.dto.DstPointDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Удалённый источник данных для операций c точками выгруза бетона.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class DstPointRemoteDataSource @Inject constructor(
    private val api: DstPointsApi
) {
    /**
     * Загружает данные точки выгруза бетона по ее коду.
     *
     * @param zoneId код точки выгрузки бетона
     * @return [DstPointDto] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getDstPoint(zoneId: Int): DstPointDto = safeApiCall { api.getDstPoints(zoneId) }
}