package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class ErrorResponseDto(
    @SerialName("ErrorNo")
    val errorNumber: Int,
    @SerialName("Message")
    val message: String,
)
