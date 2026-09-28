package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Тело запроса для авторизации.
 *
 * @property username Логин пользователя.
 * @property password Пароль пользователя.
 */
@OptIn(InternalSerializationApi::class) // <= kotlinx.serialization (1.6+)
@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
) {
}