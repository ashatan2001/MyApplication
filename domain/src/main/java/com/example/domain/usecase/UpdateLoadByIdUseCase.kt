package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.LoadNotFoundException
import com.example.domain.model.Load
import com.example.domain.model.TaskRequest
import com.example.domain.repository.QueueRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для изменения заявки на отгрузку в очереди заданного отгрузочного терминала.
 *
 * Инкапсулирует логику запроса списка заявок из [com.example.domain.repository.QueueRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы со списком заявок.
 */
class UpdateLoadByIdUseCase @Inject constructor(
    private val repository: QueueRepository
) {
    suspend operator fun invoke(loadId: String, task: TaskRequest): CustomResult<Long> {
        return try {
            val load = repository.updateLoadById(loadId, task)
            CustomResult.Success(load)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при изменении заявки $loadId в очереди отгрузочного терминала",
                    cause = e
                )
            )
        }
    }
}