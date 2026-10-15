package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные продукта от сервера.
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.ProductMapper].
 *
 * @property zone Код зоны разгрузки бетона
 * @property department Код подразделения
 * @property id Код продукта
 * @property name Наименование продукта
 * @property number Номер продукта
 * @property quantity Количество бетона, используемое по умолчанию для заказа бетона
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class ProductDto (
    @SerialName("ZoneNo") val zone: Int,
    @SerialName("DepartmentID") val department: String,
    @SerialName("ProductID") val id: Long,
    @SerialName("ProductName") val name: String,
    @SerialName("ProductNo") val number: String? = null,
    @SerialName("DefQty") val quantity: Float? = null,
)
