package com.example.domain.model

/**
 * Модель компонента.
 *
 * @property id Код компонента
 * @property name Наименование компонента
 * @property number Номер компонента
 * @property quantity Количество компонента в килограммах
 */

data class Component (
    val id: Long,
    val name: String,
    val number: String?,
    val quantity: Float?
)