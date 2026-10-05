package com.example.data.mapper

import com.example.data.dto.ArchiveDto
import com.example.data.dto.EventDto
import com.example.domain.model.Archive
import com.example.domain.model.Event
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

/**
 * Маппер события между DTO и доменной моделью.
 */
fun <T, R> ArchiveDto<T>.toDomain(itemMapper: (T) -> R): Archive<R> = Archive(
    startDate = parseDate(this.startDate),
    endDate = parseDate(this.endDate),
    pageNum = this.pageNum,
    pages = this.pages,
    records = this.records,
    data = this.data.map(itemMapper)
)

private fun parseDate(raw: String): LocalDateTime = try {
    LocalDateTime.parse(raw.trim(), DateFormatters.SERVER_DATE_TIME)
} catch (e: DateTimeParseException) {
    Timber.e(e, "Ошибка парсинга даты события: '$raw'")
    throw IllegalArgumentException("Неверный формат даты: $raw", e)
}
