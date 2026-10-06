package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.Archive
import com.example.domain.model.Event
import com.example.domain.repository.ArchiveRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


/**
 * Use-case для получения архива событий.
 *
 * Инкапсулирует логику запроса архива событий из [com.example.domain.repository.ArchiveRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы с архивами.
 */
class GetEventArchiveUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int,
    ): CustomResult<Archive<Event>> {
        return try {
            val archive = repository.getEventArchive(startDate, endDate, pageNum, records)

            CustomResult.Success(archive)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Failed to get event archive",
                    cause = e
                )
            )
        }
    }
}