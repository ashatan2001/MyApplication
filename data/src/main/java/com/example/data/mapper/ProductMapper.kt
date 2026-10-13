package com.example.data.mapper

import com.example.data.dto.ProductDto
import com.example.domain.model.Product


/**
 * Маппер продукта между DTO и доменной моделью.
 */
fun ProductDto.toDomain(): Product =
    Product(
        zone = zone,
        department = department,
        id = id,
        name = name,
        number = number,
        quantity = quantity,
    )