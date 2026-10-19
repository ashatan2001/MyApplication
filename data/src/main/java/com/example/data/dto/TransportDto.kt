package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель транспортного средства, получаемая от сервера
 *
 * @property number Номер транспортного средства
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class TransportDto (
    @SerialName("TransportNum") val number: String? = null
)
