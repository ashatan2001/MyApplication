package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные компонента от сервера.
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.ComponentMapper].
 *
 * @property productId Код компонента
 * @property productName Наименование компонента
 * @property productNum Номер компонента
 * @property density Плотность компонента
 * @property notes Примечание к компоненту
 * @property isComponent Признак того, что данный продукт является компонентом
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class ComponentDto (
    @SerialName("ProductID") val productId: Int,
    @SerialName("ProductName") val productName: String,
    @SerialName("ProductNo") val productNum: String? = null,
    @SerialName("Density") val density: Float? = null,
    @SerialName("Notes") val notes: String? = null,
    @SerialName("IsComponent") val isComponent: Boolean? = null
)
