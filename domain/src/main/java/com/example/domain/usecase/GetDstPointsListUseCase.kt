package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.DstPointNotFoundException
import com.example.domain.model.DstPoint
import com.example.domain.repository.DstPointsRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для получения списка точек выгрузки бетона.
 *
 * Инкапсулирует логику запроса списка точек выгрузки из [com.example.domain.repository.DstPointsRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы со списком точек выгрузки бетона.
 */
class GetDstPointsListUseCase @Inject constructor(
    private val repository: DstPointsRepository
) {
    suspend operator fun invoke(): CustomResult<List<DstPoint>> {
        return try {
            val dstPointsList = repository.getDstPointsList()
            CustomResult.Success(dstPointsList)
        } catch (e: CancellationException) {
            throw e
        } catch (e: DstPointNotFoundException) {
            CustomResult.Success(emptyList())
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Failed to get dst points list",
                    cause = e
                )
            )
        }
    }
}