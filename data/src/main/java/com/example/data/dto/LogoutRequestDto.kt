package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Тело запроса для выхода из системы.
 *
 * Пустой объект ([data object]), так как сервер идентифицирует
 * пользователя по кукам, а не по телу запроса.
 * Сериализуется в `{}`.
 */
@Serializable
data object LogoutRequestDto
