package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class) // <= kotlinx.serialization (1.6+)
@Serializable
data class UserDto(
    val id: Int,
    val fullname: String,
    val position: String,
    val positionId: Int,
    val isEmployee: Boolean,
    val isActive: Boolean
)