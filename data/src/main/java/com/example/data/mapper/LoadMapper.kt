package com.example.data.mapper

import LoadUpdateRequestDto
import com.example.data.dto.LoadDto
import com.example.data.dto.ProductDto
import com.example.data.dto.TerminalDto
import com.example.data.util.parseDate
import com.example.domain.model.Load
import com.example.domain.model.LoadStatus
import com.example.domain.model.LoadUpdateRequest


/**
 * Маппер заявки на отгрузку между DTO и доменной моделью.
 */
fun LoadDto.toDomain(): Load =
    Load(
        id = id,
        date = parseDate(date),
        documentNum = documentNum,
        queueNum = queueNum,
        status = LoadStatus.fromCode(status),
        statusName = statusName,
        arrivalDate = parseDate(arrivalDate),
        departDate = parseDate(departDate),
        transport = transport?.toDomain(),
        terminal = terminal?.toDomain(),
        product = product?.toDomain(),
        quality = quality,
        creator = creator?.toDomain(),
        operator = operator?.toDomain(),
        accuracyMode = accuracyMode,
        department = department?.toDomain()
    )

/**
 * Маппит доменную модель изменения заявки в DTO сервера.
 *
 * @return DTO с точными именами полей контракта.
 */
fun LoadUpdateRequest.toDto(): LoadUpdateRequestDto = LoadUpdateRequestDto(
    terminal = terminal.toDto(),
    product = product.toDto(),
    qty = quantity,
    departmentId = departmentId
)