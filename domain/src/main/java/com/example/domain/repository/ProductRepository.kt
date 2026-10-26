package com.example.domain.repository

import com.example.domain.model.Product

/**
 * Репозиторий для работы с продуктами.
 */
interface ProductRepository {
    /**
     * Возвращает список продуктов, разрешенных для отгрузки в заданную точку выгрузки.
     *
     * @param zone код точки выгрузки бетона
     * @return [List<Product>] список продуктов, разрешенных для отгрузки в заданную точку выгрузки.
     */
    suspend fun getProductList(zone: Int): List<Product>
}