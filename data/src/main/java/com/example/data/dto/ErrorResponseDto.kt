package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Тело ответа сервера при ошибке.
 *
 * Парсится в [safeApiCall] для извлечения
 * бизнес-кода ошибки и человекочитаемого сообщения.
 *
 * @property errorNumber Внутренний код ошибки сервера (поле "ErrorNo").
 *                       Может отсутствовать при инфраструктурных ошибках (5xx).
 * @property message Описание ошибки для отображения пользователю (поле "Message").
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class ErrorResponseDto(
    @SerialName("ErrorNo")
    val errorNumber: Int? = null,
    @SerialName("Message")
    val message: String? = null
)
