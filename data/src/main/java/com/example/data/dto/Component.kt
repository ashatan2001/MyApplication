package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные точки выгрузки бетона от сервера (эндпоинт `api/apb/dstpoints/:zoneid`).
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.DstPointMapper].
 *
 * @property zoneId Код зоны разгрузки бетона (поле ZoneNo).
 * @property zoneName Наименование подразделения.
 * @property departmentId Код подразделения.
 * @property postId код поста заказа продукции (операторской панели,
 *                  управляющей одной или несколькими точками выгрузки).
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class Component (
    @SerialName("ProductID") val productId: Int,
    @SerialName("ProductName") val productName: String,
    @SerialName("ProductNo") val productNum: String? = null,
    @SerialName("Density") val density: Float? = null,
    @SerialName("Notes") val notes: String? = null,
    @SerialName("IsComponent") val isComponent: Boolean? = null
)
