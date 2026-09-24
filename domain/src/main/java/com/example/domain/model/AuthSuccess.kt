package com.example.domain.model

/**
 * Результат успешной авторизации.
 *
 * @property userName Имя пользователя для отображения в приветствии.
 * @property message Приветственное сообщение от сервера.
 */
data class AuthSuccess(
    val userName: String,
    val message: String,
)
