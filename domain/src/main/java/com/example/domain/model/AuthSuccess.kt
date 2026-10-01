package com.example.domain.model

/**
 * Результат успешной авторизации.
 *
 * @property fullName Имя пользователя для отображения в приветствии.
 * @property message Приветственное сообщение от сервера.
 */
data class AuthSuccess(
    val fullName: String,
    val message: String,
)
