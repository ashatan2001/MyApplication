package com.example.data.remote

import com.example.data.dto.BucketDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для операций продуктами, разрешенными для отгрузки.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class BucketRemoteDataSource @Inject constructor(
    private val api: BucketApi
) {
    /**
     * Загружает список продуктов, разрешенных для отгрузки в заданную точку выгрузки
     *
     * @return [List<BucketDto>] при успехе.
     */
    suspend fun getBucketList(): List<BucketDto> = safeApiCall { api.getBuckets() }
}