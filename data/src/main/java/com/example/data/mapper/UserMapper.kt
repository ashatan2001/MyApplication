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
                login = dto.login,
                name = dto.name,
                role = dto.role,
            )

        fun toDto(model: UserModel): UserDto =
            UserDto(
                id = model.id,
                login = model.login,
                name = model.name,
                role = model.role,
            )
    }