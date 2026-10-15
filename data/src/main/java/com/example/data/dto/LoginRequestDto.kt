package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Тело запроса для авторизации.
 *
 * @property username Логин пользователя.
 * @property password Пароль пользователя.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
) {
}