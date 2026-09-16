package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class) // <= kotlinx.serialization (1.6+)
@Serializable
data class UserDto(
    @SerialName("PersonID") val id: Int,
    @SerialName("FIO") val userName: String,
    @SerialName("Position") val position: String,
    @SerialName("PositionID") val positionId: Int,
    @SerialName("IsEmployee") val isEmployee: Boolean,
    @SerialName("IsActive") val isActive: Boolean
)