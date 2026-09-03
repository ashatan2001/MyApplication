package com.example.data.mapper

import com.example.data.dto.LoginResponseDto
import com.example.domain.model.AuthSuccess
import javax.inject.Inject

class AuthMapper @Inject constructor() {
    fun toDomain(dto: LoginResponseDto): AuthSuccess = AuthSuccess(
        fio = dto.fio,
        message = dto.message
    )
}