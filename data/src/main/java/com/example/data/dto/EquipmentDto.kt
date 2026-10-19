package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель оборудования, получаемая от сервера
 *
 * @property id Идентификатор агрегата.
 * @property name Наименование агрегата.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class EquipmentDto (
    @SerialName("EqID") val id: Int,
    @SerialName("EqName") val name: String,
    @SerialName("EqClassName") val className: String? = null,
    @SerialName("WorkHours") val workHours: Int? = null,
    @SerialName("NOn") val nOn: Int? = null,
    @SerialName("NOff") val nOff: Int? = null,
    @SerialName("PlcID") val plcID: String? = null,
    @SerialName("PlcName") val plcName: String? = null
)
