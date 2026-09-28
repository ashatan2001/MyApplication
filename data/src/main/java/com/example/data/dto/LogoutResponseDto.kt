package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Тело ответа при успешном выходе из системы.
 *
 * @property message Приветственное сообщение от сервера (например, "Вы вышли из системы").
 */
@OptIn(InternalSerializationApi::class) // <= kotlinx.serialization (1.6+)
@Serializable
data class LogoutResponseDto(val message: String) {
}