package com.example.data.mapper

import com.example.data.dto.DstPointDto
import com.example.domain.model.DstPoint

/**
 * Маппер точки выгрузки бетона между DTO и доменной моделью.
 */
fun DstPointDto.toDomain(): DstPoint =
    DstPoint(
        zoneId = zoneId,
        zoneName = zoneName,
        departmentId = departmentId,
        postId = postId
    )