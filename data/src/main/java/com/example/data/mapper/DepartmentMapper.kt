package com.example.data.mapper

import com.example.data.dto.DepartmentDto
import com.example.data.dto.TerminalDto
import com.example.domain.model.Department
import com.example.domain.model.Terminal


/**
 * Маппер подразделения между DTO и доменной моделью.
 */
fun DepartmentDto.toDomain(): Department =
    Department(
        id = id,
        name = name
    )