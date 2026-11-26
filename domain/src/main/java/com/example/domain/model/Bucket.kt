package com.example.domain.model

import java.time.LocalDateTime

/**
 * Модель заявки на отгрузку.
 *
 * @property id Код бетоновозной телеги.
 * @property name Наименование бетоновозной телеги.
 * @property zoneNumber Номер участка, на котором находится бетоновозная телега.
 * @property isFull Признак заполненной телеги.
 * @property statuses Набор активных статусов бетонной телеги (битовая маска).
 * @property loadId Код заявки на отгрузку, которую в настоящий момент выполняет бетоновозная телега.
 * @property dstZone Номер участка, на который осуществляется доставка бетонной смеси.
 * @property loadDate Дата и время завершения приготовления бетонной смеси на бетоносмесительном узле.
 * @property arrivalDate Дата и время прибытия бетоновозной телеги в точку разгрузки.
 * @property departDate Дата и время завершения разгрузки бетоновозной телеги в точке разгрузки.
 */
data class Bucket(
    val id: Int,
    val name: String,
    val zoneNumber: Int,
    val isFull: Boolean,
    val statuses: Set<BucketStatus>,
    val loadId: Long,
    val dstZone: Int,
    val loadDate: LocalDateTime?,
    val arrivalDate: LocalDateTime?,
    val departDate: LocalDateTime?
) {
    /**
     * Возвращает приоритетный (единственный) статус для отображения в UI.
     *
     * Приоритет определяется порядком в [BucketStatus.entries]:
     * чем раньше объявлен статус, тем выше приоритет.
     *
     * @return Приоритетный статус или [BucketStatus.UNKNOWN], если набор пуст.
     */
    val primaryStatus: BucketStatus
        get() = statuses.firstOrNull() ?: BucketStatus.UNKNOWN

    /**
     * Проверяет, имеет ли телега указанный статус.
     *
     * @param status Проверяемый статус.
     * @return true, если статус присутствует в наборе.
     */
    fun hasStatus(status: BucketStatus): Boolean = status in statuses
}

/**
 * Перечисление возможных статусов бетоновозной телеги.
 *
 * Коды являются степенями двойки, что позволяет серверу передавать
 * комбинацию статусов в виде битовой маски (например, 10 = MOVING | UNLOADING).
 *
 * @property code Числовой код статуса (бит в маске), получаемый от сервера в DTO.
 */
enum class BucketStatus(val code: Int) {
    /** Неизвестный статус (используется для безопасной обработки некорректных данных от сервера). */
    UNKNOWN(0),

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
    MANUAL_CONTROL(64);

    companion object {
        /**
         * Преобразует числовую битовую маску в набор соответствующих статусов [BucketStatus].
         *
         * Каждый бит маски соответствует одному статусу. Например:
         * - код 8 → {UNLOADING}
         * - код 66 → {MOVING, MANUAL_CONTROL}
         * - код 0 → {UNKNOWN}
         *
         * @param code Числовая битовая маска статусов из DTO.
         * @return Набор распознанных статусов или {UNKNOWN}, если ни один бит не распознан.
         */
        fun fromCode(code: Int): Set<BucketStatus> {
            if (code == 0) return setOf(UNKNOWN)

            val result = entries
                .filter { it != UNKNOWN }
                .filter { (code and it.code) != 0 }
                .toSet()

            return result.ifEmpty { setOf(UNKNOWN) }
        }
    }
}