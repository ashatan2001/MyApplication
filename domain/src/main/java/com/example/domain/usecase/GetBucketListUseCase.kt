package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.DstPointNotFoundException
import com.example.domain.model.Bucket
import com.example.domain.repository.BucketRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для получения списка бетоновозных телег.
 *
 * Инкапсулирует логику запроса списка телег из [com.example.domain.repository.BucketRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы с бетоновозными телегами.
 */
class GetBucketListUseCase @Inject constructor(
    private val repository: BucketRepository
) {
    suspend operator fun invoke(): CustomResult<List<Bucket>> {
        return try {
            val bucketList = repository.getBucketList()
            CustomResult.Success(bucketList)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при получении списка телег",
                    cause = e
                )
            )
        }
    }
}