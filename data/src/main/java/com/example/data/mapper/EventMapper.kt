package com.example.data.mapper

import com.example.data.dto.EventDto
import com.example.data.util.DateFormatters
import com.example.domain.model.Event
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

/**
 * Маппер архива событий между DTO и доменной моделью.
 */

fun EventDto.toDomain(): Event =
    Event(
        id = id,
        date = parseDate(date),
        person = person.toDomain(),
        equipment = equipment.toDomain(),
        deviceName = deviceName,
        workPlaceName = workPlaceName,
        className = className,
        text = text
    )

private fun parseDate(raw: String): LocalDateTime = try {
    LocalDateTime.parse(raw.trim(), DateFormatters.SERVER_DATE_TIME)
} catch (e: DateTimeParseException) {
    Timber.e(e, "Ошибка парсинга даты: '$raw'")
    LocalDateTime.MIN
}