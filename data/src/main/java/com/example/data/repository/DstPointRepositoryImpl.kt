package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.remote.DstPointRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.exception.DstPointNotFoundException
import com.example.domain.model.DstPoint
import com.example.domain.repository.DstPointRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DstPointRepositoryImpl @Inject constructor(
    private val remoteDataSource: DstPointRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : DstPointRepository {

    override suspend fun getDstPoint(zoneId: Int): DstPoint {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении точки выгрузки бетона",
            onAuthError = { localDataSource.clearSession() },
            notFoundMapper = {
                DstPointNotFoundException(
                    zone = zoneId,
                    message = it.message ?: "Точка выгрузки бетона не найдена"
                )
            }
        ) {
            remoteDataSource.getDstPoint(zoneId)
        }
        return dto.toDomain()
    }

    override suspend fun getDstPointList(): List<DstPoint> {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении списка точек выгрузки бетона",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.getDstPointList()
        }
        return dto.map { it.toDomain() }
    }
}