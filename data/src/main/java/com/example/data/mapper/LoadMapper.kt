package com.example.data.mapper

import com.example.data.dto.LoadDto
import com.example.data.util.parseDate
import com.example.domain.model.Load
import com.example.domain.model.LoadStatus


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
        arrivalDate = arrivalDate?.let { parseDate(it) },
        departDate = departDate?.let { parseDate(it) },
        transport = transport?.toDomain(),
        terminal = terminal?.toDomain(),
        product = product?.toDomain(),
        quality = quality,
        creator = creator?.toDomain(),
        operator = operator?.toDomain(),
        accuracyMode = accuracyMode,
        department = department?.toDomain()
    )