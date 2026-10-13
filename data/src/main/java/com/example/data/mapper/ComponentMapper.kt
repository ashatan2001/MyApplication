package com.example.data.mapper

import com.example.data.dto.ComponentDto
import com.example.domain.model.Component


/**
 * Маппер компонента между DTO и доменной моделью.
 */
fun ComponentDto.toDomain(): Component =
    Component(
        id = id,
        name = name,
        number = number,
        quantity = quantity
    )