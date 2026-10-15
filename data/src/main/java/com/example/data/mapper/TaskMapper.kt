package com.example.data.mapper

import com.example.data.dto.TaskRequestDto
import com.example.data.dto.TaskResponseDto
import com.example.domain.model.TaskRequest
import com.example.domain.model.TaskResult


/**
 * Маппер создания заявки из DTO и доменной моделью.
 */
fun TaskRequest.toDto(): TaskRequestDto =
    TaskRequestDto(
        quantity = quantity,
        zone = zone,
        pallete = pallete,
        product = product
    )

fun TaskResponseDto.toDomain(): TaskResult =
    TaskResult(
        zone = zone,
        load = load,
        queueSize = queueSize,
        terminal = terminal
    )