package com.example.domain.model

/**
 * Модель продукта.
 *
 * @property zone Код зоны разгрузки бетона
 * @property department Код подразделения
 * @property id Код продукта
 * @property name Наименование продукта
 * @property number Номер продукта
 * @property quantity Количество бетона, используемое по умолчанию для заказа бетона
 */

data class Product (
    val zone: Int,
    val department: String,
    val id: Int,
    val name: String,
    val number: String?,
    val quantity: Float?,
)