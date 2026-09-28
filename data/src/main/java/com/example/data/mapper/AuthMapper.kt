package com.example.data.mapper

import com.example.data.dto.LoginResponseDto
import com.example.domain.model.AuthSuccess
import javax.inject.Inject

/**
 * Маппер результатов авторизации из DTO в доменную модель.
 *
 * Изолирует доменный слой от структуры ответа сервера:
 * при изменении контракта API правится только этот класс.
 */
class AuthMapper @Inject constructor() {
    fun toDomain(dto: LoginResponseDto): AuthSuccess = AuthSuccess(
        userName = dto.userName,
        message = dto.message
    )
}