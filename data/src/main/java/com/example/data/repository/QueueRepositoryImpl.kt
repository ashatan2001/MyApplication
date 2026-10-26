package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.mapper.toDto
import com.example.data.remote.QueueRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.model.Load
import com.example.domain.model.TaskRequest
import com.example.domain.repository.QueueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueRepositoryImpl @Inject constructor(
    private val remoteDataSource: QueueRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : QueueRepository {

    override suspend fun getLoads(): List<Load> {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении списка заявок отгрузки",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.getLoads()
        }
        return dto.map { it.toDomain() }
    }

    override suspend fun getLoadsByTerminalId(terminalId: String): List<Load> {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении списка заявок отгрузки",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.getLoadsByTerminalId(terminalId)
        }
        return dto.map { it.toDomain() }
    }

    override suspend fun getLoadsByTerminalNum(terminalNum: Int): List<Load> {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении списка заявок отгрузки",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.getLoadsByTerminalNum(terminalNum)
        }
        return dto.map { it.toDomain() }
    }

    override suspend fun updateLoadById(loadId: String, task: TaskRequest): Long {
        val requestDto = task.toDto()
        val responseDto = safeRepositoryCall(
            contextMessage = "Ошибка при создании заявки",
            onAuthError = { localDataSource.clearSession() },
        ) {
            remoteDataSource.updateLoadById(loadId, requestDto)
        }
        return responseDto.id
    }

    override suspend fun updateQueueNumLoadById(loadId: String, number: Int): Long {
        val responseDto = safeRepositoryCall(
            contextMessage = "Ошибка при создании заявки",
            onAuthError = { localDataSource.clearSession() },
        ) {
            remoteDataSource.updateQueueNumLoadById(loadId, number)
        }
        return responseDto.id
    }

    override suspend fun deleteLoadById(loadId: String) {
        safeRepositoryCall(
            contextMessage = "Ошибка при удалении заявки",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.deleteLoadById(loadId)
        }
    }
}