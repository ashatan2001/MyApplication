package com.example.data.repository

import com.example.data.mapper.toDomain
import com.example.data.remote.ArchiveRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.model.Archive
import com.example.domain.model.Event
import com.example.domain.model.HandLoad
import com.example.domain.repository.ArchiveRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveRepositoryImpl @Inject constructor(
    private val remoteDataSource: ArchiveRemoteDataSource
) : ArchiveRepository {

    override suspend fun getEventArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int,
    ): Archive<Event> {
        val dto = safeRepositoryCall(contextMessage = "Ошибка при получении архива событий") {
            remoteDataSource.getEventArchive(startDate, endDate, pageNum, records)
        }
        return dto.toDomain { it.toDomain() }
    }

    override suspend fun getHandLoadArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int,
    ): Archive<HandLoad> {
        val dto = safeRepositoryCall(contextMessage = "Ошибка при получении архива ручных отгрузок") {
            remoteDataSource.getHandLoadArchive(startDate, endDate, pageNum, records)
        }
        return dto.toDomain { it.toDomain() }
    }
}