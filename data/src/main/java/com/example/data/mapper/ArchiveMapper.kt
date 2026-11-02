package com.example.data.mapper

import com.example.data.dto.ArchiveDto
import com.example.data.util.DateFormatters
import com.example.data.util.parseDate
import com.example.domain.model.Archive
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

/**
 * Маппер архива между DTO и доменной моделью.
 */
fun <T, R> ArchiveDto<T>.toDomain(itemMapper: (T) -> R): Archive<R> = Archive(
    startDate = parseDate(startDate),
    endDate = parseDate(endDate),
    pageNum = pageNum,
    pages = pages,
    records = records,
    data = data.map(itemMapper)
)