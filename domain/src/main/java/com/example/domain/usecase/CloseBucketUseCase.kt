package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.repository.BucketRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для отправки команды завершения разгрузки бетоновозной телеги.
 *
 * Инкапсулирует логику запроса из [BucketRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы с бетоновозными телегами.
 */
class CloseBucketUseCase @Inject constructor(
    private val repository: BucketRepository
) {
    /**
     * Отправляет команду завершения разгрузки бетоновозной телеги.
     *
     * @param id Код бетоновозной телеги.
     * @return [CustomResult.Success] при успехе или [CustomResult.Error] при ошибке.
     */
    suspend operator fun invoke(id: Int): CustomResult<Unit> {
        return try {
            repository.closeBucket(id)
            CustomResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при отправке команды завершения разгрузки телеги",
                    cause = e
                )
            )
        }
    }
}