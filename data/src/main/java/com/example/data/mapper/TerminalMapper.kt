package com.example.data.mapper

import com.example.data.dto.TerminalDto
import com.example.domain.model.Terminal


/**
 * Маппер терминала между DTO и доменной моделью.
 */
fun TerminalDto.toDomain(): Terminal =
    Terminal(
        id = id,
        number = number,
        name = name,
        measure = measure,
        type = type
    )