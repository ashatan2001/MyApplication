package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные компонента от сервера.
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.ComponentMapper].
 *
 * @property id Код компонента
 * @property number Номер компонента
 * @property name Наименование компонента
 * @property quantity Количество компонента в килограммах
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class ComponentDto (
    @SerialName("CompID") val id: Int,
    @SerialName("CompNo") val number: String,
    @SerialName("CompName") val name: String,
    @SerialName("CompQty") val quantity: Float,
)
