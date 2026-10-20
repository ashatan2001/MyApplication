package com.example.data.mapper

import com.example.data.dto.EventDto
import com.example.data.util.parseDate
import com.example.domain.model.Event

/**
 * Маппер архива событий между DTO и доменной моделью.
 */

fun EventDto.toDomain(): Event =
    Event(
        id = id,
        date = parseDate(date),
        person = person?.toDomain(),
        equipment = equipment?.toDomain(),
        deviceName = deviceName,
        workPlaceName = workPlaceName,
        className = className,
        text = text
    )

