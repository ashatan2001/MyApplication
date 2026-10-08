package com.example.domain.model

/**
 * Модель профиля пользователя в системе.
 *
 * @property id Уникальный идентификатор пользователя.
 * @property fullName ФИО пользователя.
 * @property position Название должности.
 * @property positionId ID должности для связи со справочником.
 * @property isEmployee true, если пользователь является сотрудником организации.
 * @property isActive true, если учётная запись не заблокирована.
 */
data class Person(
    val id: Int,
    val fullName: String,
    val position: String?,
    val positionId: Int?,
    val isEmployee: Boolean?,
    val isActive: Boolean?,
)
