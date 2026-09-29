package com.example.data.mapper

import com.example.data.dto.UserDto
import com.example.domain.model.User
import javax.inject.Inject

/**
 * Маппер пользователя между DTO и доменной моделью.
 *
 * Двунаправленный: используется как для чтения данных (сервер → домен),
 * так и для записи в кэш (домен → DTO).
 */
class UserMapper @Inject constructor() {
        fun toModel(dto: UserDto): User =
            User(
                id = dto.id,
                userName = dto.userName,
                position = dto.position,
                positionId = dto.positionId,
                isEmployee = dto.isEmployee,
                isActive = dto.isActive,
            )

        fun toDto(model: User): UserDto =
            UserDto(
                id = model.id,
                userName = model.userName,
                position = model.position,
                positionId = model.positionId,
                isEmployee = model.isEmployee,
                isActive = model.isActive,
            )
    }