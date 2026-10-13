package com.example.data.remote

import com.example.data.dto.ProductDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для операций продуктами, разрешенными для отгрузки.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class ProductRemoteDataSource @Inject constructor(
    private val api: ProductApi
) {
    /**
     * Загружает список продуктов, разрешенных для отгрузки в заданную точку выгрузки
     *
     * @param zone код точки выгрузки бетона
     * @return [List<ProductDto>] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getProductList(zone: Int): List<ProductDto> = safeApiCall { api.getProductList(zone) }
}