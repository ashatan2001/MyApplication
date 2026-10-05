package com.example.data.repository

import com.example.data.dto.ArchiveDto
import com.example.data.dto.EventDto
import com.example.data.exception.DataLayerException
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.remote.ArchiveRemoteDataSource
import com.example.domain.exception.AppException
import com.example.domain.model.Archive
import com.example.domain.model.Event
import com.example.domain.repository.ArchiveRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveRepositoryImpl @Inject constructor(
    private val remoteDataSource: ArchiveRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : ArchiveRepository {

    override suspend fun getEventArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        pages: Int,
        records: Int,
    ): Archive<Event> {
        val dto: ArchiveDto<EventDto> = try {
            remoteDataSource.getEventArchive(startDate, endDate, pageNum, pages, records)
        } catch (e: DataLayerException) {
            throw e
        } catch (e: Exception) {
            throw AppException(
                message = "Неожиданная ошибка при получении архива событий",
                cause = e
            )
        }

        return dto.toDomain { eventDto -> eventDto.toDomain() }
    }
}