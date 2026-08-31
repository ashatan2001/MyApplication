package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class) // <= kotlinx.serialization (1.6+)
@Serializable
class LoginRequestDto(
    val username: String,
    val password: String
) {
}