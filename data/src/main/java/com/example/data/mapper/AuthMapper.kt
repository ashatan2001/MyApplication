package com.example.data.mapper

import com.example.data.dto.LoginResponseDto
import com.example.domain.model.AuthSuccess

/**
 * Маппер результатов авторизации из DTO в доменную модель.
 */
fun LoginResponseDto.toDomain(): AuthSuccess =
    AuthSuccess(
        fullName = fullName,
        message = message
    )