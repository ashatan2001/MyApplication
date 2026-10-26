package com.example.domain.repository

import com.example.domain.model.TaskRequest
import com.example.domain.model.TaskResult

/**
 * Репозиторий для работы с заявками на производство ЖБИ.
 */
interface TaskRepository {
    /**
     * Добавляет заявку в очередь на отгрузку.
     *
     * @param task данные добавляемой заявки
     * @return [TaskResult] ответ сервера при успешном создании заявки.
     */
    suspend fun createTask(task: TaskRequest): TaskResult
}