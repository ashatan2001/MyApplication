package com.example.data.mapper

import com.example.data.dto.HandLoadDto
import com.example.data.util.DateFormatters
import com.example.domain.model.HandLoad
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

/**
 * Маппер архива ручных загрузок между DTO и доменной моделью.
 */

fun HandLoadDto.toDomain(): HandLoad =
    HandLoad(
        id = id,
        date = parseDate(date),
        workPlaceName = workPlaceName,
        status = status,
        equipment = equipment.toDomain(),
        equipmentCapacity = equipmentCapacity,
        component = component.toDomain()
    )

private fun parseDate(raw: String): LocalDateTime = try {
    LocalDateTime.parse(raw.trim(), DateFormatters.SERVER_DATE_TIME)
} catch (e: DateTimeParseException) {
    Timber.e(e, "Ошибка парсинга даты: '$raw'")
    LocalDateTime.MIN
}