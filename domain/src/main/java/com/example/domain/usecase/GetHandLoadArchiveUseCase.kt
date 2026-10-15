package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.Archive
import com.example.domain.model.HandLoad
import com.example.domain.repository.ArchiveRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


/**
 * Use-case для получения архива ручных отгрузок.
 *
 * Инкапсулирует логику запроса архива ручных отгрузок из [com.example.domain.repository.ArchiveRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы с архивами.
 */
class GetHandLoadArchiveUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int,
    ): CustomResult<Archive<HandLoad>> {
        return try {
            val archive = repository.getHandLoadArchive(startDate, endDate, pageNum, records)

            CustomResult.Success(archive)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при получении архива ручных отгрузок",
                    cause = e
                )
            )
        }
    }
}