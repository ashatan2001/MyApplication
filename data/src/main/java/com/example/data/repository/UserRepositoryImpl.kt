package com.example.data.repository

import android.util.Log
import com.example.data.exception.DataException
import com.example.data.exception.DataLayerException
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.UserMapper
import com.example.data.remote.UserRemoteDataSource
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val mapper: UserMapper
) : UserRepository {
    private val userCache = ConcurrentHashMap<Int, UserModel>()

    override suspend fun getUserId(): Int? {
        return localDataSource.getUserId()
    }

    override suspend fun getUserName(): String? = localDataSource.getUserName()


    override suspend fun getUserInfo(): UserModel {
        val userId = getUserId()
            ?: throw IllegalStateException("Пользователь не авторизирован")

        userCache[userId]?.let { return it }

        return try {
            val dto = remoteDataSource.getUserInfo(userId)
            mapper.toModel(dto).also { userCache[userId] = it }

        } catch (e: DataLayerException) {
            if (e.errorCode == 404) {
                throw UserNotFoundException(
                    userId = userId,
                    message = "Пользователь не найден на сервере"
                )
            }

            // Для остальных сетевых ошибок (500, 502, таймаут и т.д.)
            throw DataException(
                message = "Ошибка сети при получении данных пользователя (код: ${e.errorCode})",
                cause = e
            )
        } catch (e: UserNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw DataException(
                message = "Неожиданная ошибка при получении пользователя $userId: ${e.message}",
                cause = e
            )
        }
    }

    override fun clearCache() {
        userCache.clear()
        Log.d("UserRepository", "User cache cleared")
    }
}