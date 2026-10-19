package com.example.data.mapper

import com.example.data.dto.TransportDto
import com.example.domain.model.Transport


/**
 * Маппер терминала между DTO и доменной моделью.
 */
fun TransportDto.toDomain(): Transport =
    Transport(
        number = number
    )