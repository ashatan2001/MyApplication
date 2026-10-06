package com.example.domain.model

import java.time.LocalDateTime

/**
 * Модель архива в автоматизированной системы.
 *
 * @property startDate Дата и время начала периода
 * @property endDate Дата и время окончания периода
 * @property pageNum Номер текущей просматриваемой страницы с данными.
 * @property pages Общее количество страниц, которые доступны за выбранный промежуток времени.
 * @property records Количество записей на одну страницу
 * @property data Данные из архива
 */
data class Archive<T>(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val pageNum: Int,
    val pages: Int,
    val records: Int,
    val data: List<T>
) {
    val hasNextPage: Boolean
        get() = pageNum < pages

    val nextPage: Int?
        get() = if (hasNextPage) pageNum + 1 else null
}