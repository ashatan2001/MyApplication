package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.LoadNotFoundException
import com.example.domain.model.Load
import com.example.domain.repository.QueueRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для получения списка заявок на отгрузку, находящихся в очереди заданного отгрузочного терминала.
 *
 * Инкапсулирует логику запроса списка заявок из [com.example.domain.repository.QueueRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы со списком заявок.
 */
class GetLoadQueueByTerminalIdUseCase @Inject constructor(
    private val repository: QueueRepository
) {
    suspend operator fun invoke(terminalId: String): CustomResult<List<Load>> {
        return try {
            val loads = repository.getLoadsByTerminalId(terminalId)
            CustomResult.Success(loads)
        } catch (e: CancellationException) {
            throw e
        } catch (e: LoadNotFoundException) {
            CustomResult.Success(emptyList())
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при получении списка заявок в очереди отгрузочного терминала $terminalId",
                    cause = e
                )
            )
        }
    }
}