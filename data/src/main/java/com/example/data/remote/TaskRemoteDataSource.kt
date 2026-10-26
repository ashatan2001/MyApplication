package com.example.data.remote

import com.example.data.dto.TaskRequestDto
import com.example.data.dto.TaskResponseDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для операций с заявками на отгрузку.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class TaskRemoteDataSource @Inject constructor(
    private val api: TaskApi
) {
    /**
     * Создает заявку на отгрузку
     *
     * @param task данные заявки
     * @return [TaskResponseDto] при успехе.
     */
    suspend fun createTask(task: TaskRequestDto): TaskResponseDto = safeApiCall { api.createTask(task) }
}