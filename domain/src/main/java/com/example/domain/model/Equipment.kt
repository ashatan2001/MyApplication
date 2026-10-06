package com.example.domain.model

/**
 * Модель оборудования.
 *
 * @property id Идентификатор агрегата.
 * @property name Наименование агрегата.
 */
class Equipment(
    val id: Int,
    val name: String,
    val eqClassName: String?,
    val workHours: Int?,
    val nOn: Int?,
    val nOff: Int?,
    val plcID: String?,
    val plcName: String?
)