package com.example.data.mapper

import com.example.data.dto.PersonDto
import com.example.domain.model.Person

/**
 * Маппер пользователя между DTO и доменной моделью.
 *
 * Двунаправленный: используется как для чтения данных (сервер → домен),
 * так и для записи в кэш (домен → DTO).
 */
fun PersonDto.toDomain(): Person =
    Person(
        id = id,
        fullName = fullName,
        position = position,
        positionId = positionId,
        isEmployee = isEmployee,
        isActive = isActive
    )