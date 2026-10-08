package com.example.data.mapper

import com.example.data.dto.EquipmentDto
import com.example.domain.model.Equipment

/**
 * Маппер оборудования между DTO и доменной моделью.
 *
 * Двунаправленный: используется как для чтения данных (сервер → домен),
 * так и для записи в кэш (домен → DTO).
 */
fun EquipmentDto.toDomain(): Equipment =
    Equipment(
        id = id,
        name = name,
        eqClassName = eqClassName,
        workHours = workHours,
        nOn = nOn,
        nOff = nOff,
        plcID = plcID,
        plcName = plcName
    )