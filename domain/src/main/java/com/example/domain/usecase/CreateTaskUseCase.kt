package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.TaskRequest
import com.example.domain.model.TaskResult
import com.example.domain.repository.TaskRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для создания заявок на производство ЖБИ.
 *
 * Инкапсулирует логику создания заявок [com.example.domain.repository.TaskRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для создания заявок на производство ЖБИ.
 */
class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: TaskRequest): CustomResult<TaskResult> {
        return try {
            val dstPointList = repository.createTask(task)
            CustomResult.Success(dstPointList)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при создании заявки",
                    cause = e
                )
            )
        }
    }
}