package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные ручной отгрузки из архива системы.
 *
 * @property id Идентификатор ручной отгрузки в базе данных.
 * @property date Дата и время возникновения события.
 * @property workPlaceName Наименование рабочего места.
 * @property status Признак достоверной или недостоверной отгрузки.
 * @property equipment Информация об оборудовании.
 * @property equipmentCapacity Вместимость дозатора в килограммах.
 * @property component Информация о компоненте
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class HandLoadDto (
    @SerialName("LoadID") val id: Int,
    @SerialName("LoadDate") val date: String,
    @SerialName("WPName") val workPlaceName: String? = null,
    @SerialName("Status") val status: Boolean? = null,
    @SerialName("Eq") val equipment: EquipmentDto? = null,
    @SerialName("EqCapacity") val equipmentCapacity: Float,
    @SerialName("Comp") val component: Component,
)
