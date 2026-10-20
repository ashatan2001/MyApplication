package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель подразделения, получаемая от сервера
 *
 * @property id Код подразделения.
 * @property name Наименование подразделения.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class DepartmentDto (
    @SerialName("DepartmentID") val id: String,
    @SerialName("DepartmentName") val name: String? = null
)
