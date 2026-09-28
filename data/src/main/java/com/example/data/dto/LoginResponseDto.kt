package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Тело ответа при успешной авторизации.
 *
 * @property userName Отображаемое имя пользователя.
 *                    В JSON приходит как "fio" (историческое название поля на сервере).
 * @property message Приветственное сообщение от сервера.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class LoginResponseDto(
    @SerialName("fio")
    val userName: String,
    val message: String
) {
}