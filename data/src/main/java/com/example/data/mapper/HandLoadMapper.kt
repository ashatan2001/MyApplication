package com.example.data.mapper

import com.example.data.dto.HandLoadDto
import com.example.data.util.parseDate
import com.example.domain.model.HandLoad

/**
 * Маппер ручной загрузки между DTO и доменной моделью.
 * Безопасно обрабатывает отсутствие данных об оборудовании или компоненте.
 */
fun HandLoadDto.toDomain(): HandLoad = HandLoad(
    id = id,
    date = parseDate(date),
    workPlaceName = workPlaceName,
    status = status,
    equipment = equipment?.toDomain(),
    equipmentCapacity = equipmentCapacity,
    component = component?.toDomain()
)