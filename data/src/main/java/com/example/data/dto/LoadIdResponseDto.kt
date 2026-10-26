package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель при изменении заявки на отгрузку, получаемая от сервера.
 *
 * @property id Код заявки на отгрузку.

 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class LoadIdResponseDto(
    @SerialName("LoadID") val id: Long,
)

