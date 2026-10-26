package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.repository.QueueRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для удаления заявки на отгрузку из очереди заданного отгрузочного терминала.
 *
 * Инкапсулирует логику удаления заявки через [com.example.domain.repository.QueueRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы со списком заявок.
 */
class DeleteLoadByIdUseCase @Inject constructor(
    private val repository: QueueRepository
) {
    suspend operator fun invoke(loadId: String): CustomResult<Unit> {
        return try {
            repository.deleteLoadById(loadId)
            CustomResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при удалении заявки $loadId из очереди отгрузочного терминала",
                    cause = e
                )
            )
        }
    }
}