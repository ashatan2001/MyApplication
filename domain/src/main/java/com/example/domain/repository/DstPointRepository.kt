package com.example.domain.repository

import com.example.domain.model.DstPoint

interface DstPointRepository {
    suspend fun getDstPoint(zoneId: Int): DstPoint
}