package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель пользователя, получаемая от сервера
 *
 * @property id Уникальный идентификатор пользователя.
 * @property fullName ФИО пользователя (в серверном ответе — "FIO").
 * @property position Название должности.
 * @property positionId ID должности.
 * @property isEmployee Признак сотрудника организации.
 * @property isActive Признак активной учётной записи.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class PersonDto(
    @SerialName("PersonID") val id: String,
    @SerialName("FIO") val fullName: String,
    @SerialName("Position") val position: String? = null,
    @SerialName("PositionID") val positionId: Int? = null,
    @SerialName("IsEmployee") val isEmployee: Boolean? = null,
    @SerialName("IsActive") val isActive: Boolean? = null
)