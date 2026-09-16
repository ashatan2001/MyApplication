package com.example.data.mapper

import com.example.data.dto.UserDto
import com.example.domain.model.UserModel
import javax.inject.Inject

class UserMapper
    @Inject
    constructor() {
        fun toModel(dto: UserDto): UserModel =
            UserModel(
                id = dto.id,
                userName = dto.userName,
                position = dto.position,
                positionId = dto.positionId,
                isEmployee = dto.isEmployee,
                isActive = dto.isActive,
            )

        fun toDto(model: UserModel): UserDto =
            UserDto(
                id = model.id,
                userName = model.userName,
                position = model.position,
                positionId = model.positionId,
                isEmployee = model.isEmployee,
                isActive = model.isActive,
            )
    }