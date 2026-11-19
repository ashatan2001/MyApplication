package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.LoadNotFoundException
import com.example.domain.model.Load
import com.example.domain.repository.QueueRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для получения заявки на отгрузку по ее коду.
 *
 * Инкапсулирует логику запроса списка заявок из [com.example.domain.repository.QueueRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы со списком заявок.
 */
class GetLoadByIdUseCase @Inject constructor(
    private val repository: QueueRepository
) {
    suspend operator fun invoke(loadId: Long): CustomResult<Load> {
        return try {
            val load = repository.getLoads().find { it.id == loadId }
            if (load != null) {
                CustomResult.Success(load)
            } else {
                CustomResult.Error(
                    LoadNotFoundException("Заявка №$loadId не найдена в очереди")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: LoadNotFoundException) {
            CustomResult.Error(
                LoadNotFoundException("Заявка №$loadId не найдена в очереди", e)
            )
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при получении заявки",
                    cause = e
                )
            )
        }
    }
}