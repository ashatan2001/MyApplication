package com.example.domain.model

/**
 * Модель терминала.
 *
 * @property id Код терминала
 * @property number Номер терминала
 * @property name Наименование терминала
 * @property measure Единица измерения терминала
 * @property type Тип терминала
 */

data class Terminal (
    val id: String,
    val number: Int,
    val name: String,
    val measure: String?,
    val type: Int,
)