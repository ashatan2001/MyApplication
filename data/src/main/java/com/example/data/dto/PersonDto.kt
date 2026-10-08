package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные пользователя от сервера (эндпоинт `api/persons/{id}`).
 *
 * Сервер возвращает поля с именами в стиле PascalCase и историческими
 * названиями ("FIO" вместо "userName", "PersonID" вместо "id").
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.PersonMapper].
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
    @SerialName("PersonID") val id: Int,
    @SerialName("FIO") val fullName: String,
    @SerialName("Position") val position: String? = null,
    @SerialName("PositionID") val positionId: Int? = null,
    @SerialName("IsEmployee") val isEmployee: Boolean? = null,
    @SerialName("IsActive") val isActive: Boolean? = null
)