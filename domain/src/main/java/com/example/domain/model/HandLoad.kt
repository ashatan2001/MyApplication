package com.example.domain.model

import java.time.LocalDateTime

/**
 * Данные ручной отгрузки из архива системы.
 *
 * @property id Идентификатор ручной отгрузки в базе данных.
 * @property date Дата и время возникновения события.
 * @property workPlaceName Наименование рабочего места.
 * @property status Признак достоверной или недостоверной отгрузки.
 * @property equipment Информация об оборудовании.
 * @property equipmentCapacity Вместимость дозатора в килограммах.
 * @property component Информация о компоненте
 */

data class HandLoad (
    val id: Int,
    val date: LocalDateTime,
    val workPlaceName: String?,
    val status: Boolean?,
    val equipment: Equipment?,
    val equipmentCapacity: Float?,
    val component: Component?
)