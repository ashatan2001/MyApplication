package com.example.data.mapper

import com.example.data.dto.PersonDto
import com.example.domain.model.Person
import javax.inject.Inject

/**
 * Маппер пользователя между DTO и доменной моделью.
 *
 * Двунаправленный: используется как для чтения данных (сервер → домен),
 * так и для записи в кэш (домен → DTO).
 */
class PersonMapper @Inject constructor() {
        fun toModel(dto: PersonDto): Person =
            Person(
                id = dto.id,
                fullName = dto.fullName,
                position = dto.position,
                positionId = dto.positionId,
                isEmployee = dto.isEmployee,
                isActive = dto.isActive,
            )

        fun toDto(model: Person): PersonDto =
            PersonDto(
                id = model.id,
                fullName = model.fullName,
                position = model.position,
                positionId = model.positionId,
                isEmployee = model.isEmployee,
                isActive = model.isActive,
            )
    }