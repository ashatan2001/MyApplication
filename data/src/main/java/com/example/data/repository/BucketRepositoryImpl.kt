package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.remote.BucketRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.model.Bucket
import com.example.domain.repository.BucketRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BucketRepositoryImpl @Inject constructor(
    private val remoteDataSource: BucketRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : BucketRepository {

    override suspend fun getBucketList(): List<Bucket> {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении списка бетоновозных телег",
            onAuthError = { localDataSource.clearSession() },
        ) {
            remoteDataSource.getBucketList()
        }
        return dto.map { it.toDomain() }
    }

    override suspend fun openBucket(id: Int) {
        safeRepositoryCall(
            contextMessage = "Ошибка при отправке команды начала разгрузки телеги",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.openBucket(id)
        }
    }

    override suspend fun closeBucket(id: Int) {
        safeRepositoryCall(
            contextMessage = "Ошибка при отправке команды завершения разгрузки телеги",
            onAuthError = { localDataSource.clearSession() }
        ) {
            remoteDataSource.closeBucket(id)
        }
    }
}