package com.example.data.mapper

import com.example.data.dto.DstPointDto
import com.example.domain.model.DstPoint
import javax.inject.Inject

/**
 * Маппер точки загрузки бетона между DTO и доменной моделью.
 */
class DstPointMapper @Inject constructor() {

    fun toModel(dto: DstPointDto): DstPoint =
        DstPoint(
            zoneId = dto.zoneId,
            zoneName = dto.zoneName,
            departmentId = dto.departmentId,
            postId = dto.postId
        )

    fun toDomain(list: List<DstPointDto>): List<DstPoint> =
            list.map(::toModel)

    fun toDto(domain: DstPoint): DstPointDto =
        DstPointDto(
            zoneId = domain.zoneId,
            zoneName = domain.zoneName,
            departmentId = domain.departmentId,
            postId = domain.postId
        )
}