package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.mapper.toDto
import com.example.data.remote.TaskRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.model.TaskRequest
import com.example.domain.model.TaskResult
import com.example.domain.repository.TaskRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val remoteDataSource: TaskRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : TaskRepository {

    override suspend fun createTask(task: TaskRequest): TaskResult {
        val requestDto = task.toDto()
        val responseDto = safeRepositoryCall(
            contextMessage = "Ошибка при получении точки выгрузки бетона",
            onAuthError = { localDataSource.clearSession() },
        ) {
            remoteDataSource.createTask(requestDto)
        }
        return responseDto.toDomain()
    }
}