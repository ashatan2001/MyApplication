package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные оборудования.
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.EquipmentMapper].
 *
 * @property id Идентификатор агрегата.
 * @property name Наименование агрегата.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class EquipmentDto (
    @SerialName("EqID") val id: Int,
    @SerialName("EqName") val name: String,
)
