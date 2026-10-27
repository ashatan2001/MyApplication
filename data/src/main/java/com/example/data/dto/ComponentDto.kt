package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель компонента, получаемая от сервера
 *
 * @property id Код компонента
 * @property number Номер компонента
 * @property name Наименование компонента
 * @property quantity Количество компонента в килограммах
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class ComponentDto (
    @SerialName("CompID") val id: Long,
    @SerialName("CompNo") val number: String? = null,
    @SerialName("CompName") val name: String? = null,
    @SerialName("CompQty") val quantity: Float? = null,
)
