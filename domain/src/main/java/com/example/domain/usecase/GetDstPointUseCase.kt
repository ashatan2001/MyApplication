package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.DstPoint
import com.example.domain.repository.DstPointRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

/**
 * Use-case для получения информации о конкретной точки выгрузке бетона.
 *
 * Инкапсулирует логику запроса профиля из [com.example.domain.model.DstPointRepository] и оборачивает результат
 * в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы с данными точки выгрузки бетона.
 */
class GetDstPointUseCase @Inject constructor(
    private val repository: DstPointRepository
) {
    suspend operator fun invoke(zoneId: Int): CustomResult<DstPoint> {
        return try {
            val dstPoint = repository.getDstPoint(zoneId)
            CustomResult.Success(dstPoint)
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(AppException(message = "Failed to get current dst point", cause = e))
        }
    }
}