package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.remote.DstPointsRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.exception.DstPointNotFoundException
import com.example.domain.model.DstPoint
import com.example.domain.repository.DstPointsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DstPointsRepositoryImpl @Inject constructor(
    private val remoteDataSource: DstPointsRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : DstPointsRepository {

    override suspend fun getDstPoint(zoneId: Int): DstPoint {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении точки выгрузки бетона",
            onAuthError = { localDataSource.clearSession() },
            notFoundMapper = {
                DstPointNotFoundException(
                    zoneId = zoneId,
                    message = it.message ?: "Точка выгрузки бетона не найдена"
                )
            }
        ) {
            remoteDataSource.getDstPoint(zoneId)
        }
        return dto.toDomain()
    }

    override suspend fun getDstPointsList(): List<DstPoint> {
        val dtoList = safeRepositoryCall(
            contextMessage = "Ошибка при получении списка точек выгрузки бетона",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.getDstPointsList()
        }
        return dtoList.map { it.toDomain() }
    }
}