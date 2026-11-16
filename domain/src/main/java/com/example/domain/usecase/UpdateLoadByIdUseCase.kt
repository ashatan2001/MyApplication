package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.LoadUpdateRequest
import com.example.domain.repository.QueueRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для изменения заявки на отгрузку в очереди заданного отгрузочного терминала.
 *
 * Оборачивает результат [QueueRepository] в [CustomResult] для безопасной
 * обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы со списком заявок.
 */
class UpdateLoadByIdUseCase @Inject constructor(
    private val repository: QueueRepository
) {
    /**
     * Изменяет заявку в очереди терминала.
     *
     * @param loadId Код изменяемой заявки.
     * @param request Новые данные заявки.
     * @return Код изменённой заявки либо ошибка.
     */
    suspend operator fun invoke(loadId: String, request: LoadUpdateRequest): CustomResult<Long> {
        return try {
            CustomResult.Success(repository.updateLoadById(loadId, request))
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