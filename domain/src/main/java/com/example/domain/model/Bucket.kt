package com.example.domain.model

import java.time.LocalDateTime

/**
 * Модель заявки на отгрузку.
 *
 * @property id Код бетоновозной телеги.
 * @property name Наименование бетоновозной телеги.
 * @property zoneNumber Номер участка, на котором находится бетоновозная телега.
 * @property isFull Признак заполненной телеги.
 * @property status Статус бетонной телеги.
 * @property loadId Код заявки на отгрузку, которую в настоящий момент выполняет бетоновозная телега.
 * @property dstZone Номер участка, на который осуществляется доставка бетонной смеси.
 * @property loadDate Дата и время завершения приготовления бетонной смеси на бетоносмесительном узле.
 * @property arrivalDate Дата и время прибытия бетоновозной телеги в точку разгрузки
 * @property departDate Дата и время завершения разгрузки бетоновозной телеги в точке разгрузки
 */

data class Bucket(
    val id: Int,
    val name: String,
    val zoneNumber: Int,
    val isFull: Boolean,
    val status: BucketStatus?,
    val loadId: Long,
    val dstZone: Int,
    val loadDate: LocalDateTime?,
    val arrivalDate: LocalDateTime?,
    val departDate: LocalDateTime?
)

/**
 * Перечисление возможных статусов бетоновозной телеги.
 *
 * @property code Числовой код статуса, получаемый от сервера в DTO.
 */
enum class BucketStatus(val code: Int) {
    /** Телега остановлена и находится в ожидании загрузки под смесителем. */
    WAITING_FOR_LOADING(1),

    /** Телега движется. */
    MOVING(2),

    /** Телега остановлена и зафиксирована в произвольной точке. */
    STOPPED_AT_POINT(4),

    /** Идет разгрузка телеги. */
    UNLOADING(8),

    /** Телега отключена (нет связи с телегой). */
    DISCONNECTED(16),

    /** Телега находится на ремонте. */
    UNDER_REPAIR(32),

    /** Телега находится на ручном управлении. */
    MANUAL_CONTROL(64),

    /** Неизвестный статус (используется для безопасной обработки некорректных данных от сервера). */
    UNKNOWN(0);

    companion object {
        /**
         * Преобразует числовой код в соответствующий статус [BucketStatus].
         *
         * @param code Числовой код статуса из DTO.
         * @return Найденный статус или [UNKNOWN], если код не распознан.
         */
        fun fromCode(code: Int): BucketStatus {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}