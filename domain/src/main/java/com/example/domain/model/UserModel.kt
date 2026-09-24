package com.example.domain.model

/**
 * Модель профиля пользователя в системе.
 *
 * @property id Уникальный идентификатор пользователя.
 * @property userName Отображаемое имя пользователя.
 * @property position Название должности (например, "Инженер").
 * @property positionId ID должности для связи со справочником.
 * @property isEmployee true, если пользователь является сотрудником организации.
 * @property isActive true, если учётная запись не заблокирована.
 */
data class UserModel(
    val id: Int,
    // val login: String,
    val userName: String,
    val position: String,
    val positionId: Int,
    val isEmployee: Boolean,
    val isActive: Boolean,
)
