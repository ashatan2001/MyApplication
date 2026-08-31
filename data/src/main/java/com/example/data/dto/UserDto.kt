package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class) // <= kotlinx.serialization (1.6+)
@Serializable
data class UserDto(
    val id: Int,
    val login: String,
    val name: String,
    val role: String,
)