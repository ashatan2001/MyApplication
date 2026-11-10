package com.example.data.mapper

import com.example.data.dto.BucketDto
import com.example.data.util.parseDate
import com.example.domain.model.Bucket
import com.example.domain.model.BucketStatus

/**
 * Расширение для преобразования [BucketDto] в доменную модель [Bucket].
 *
 * @return Доменная модель [Bucket], готовая для использования в бизнес-логике.
 */
fun BucketDto.toDomain(): Bucket {
    return Bucket(
        id = id,
        name = name,
        zoneNumber = zoneNumber,
        isFull = isFull,
        status = BucketStatus.fromCode(status),
        loadId = loadId,
        dstZone = dstZone,
        loadDate = parseDate(loadDate),
        arrivalDate = parseDate(arrivalDate),
        departDate = parseDate(departDate),
    )
}