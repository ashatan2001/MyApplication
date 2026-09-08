package com.example.data.repository

import android.util.Log
import com.example.data.exception.DataException
import com.example.data.exception.NetworkException
import com.example.data.local.UserLocalDataSource
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
    private val localDataSource: UserLocalDataSource,
    private val mapper: UserMapper
) : UserRepository {
    private val userCache = ConcurrentHashMap<Int, UserModel>()

    override suspend fun getUser(id: Int): UserModel {
        userCache[id]?.let { return it }

        return try {
            val dto = remoteDataSource.getUser(id)
            mapper.toModel(dto).also { userCache[id] = it }

        } catch (e: UserNotFoundException) {
            throw e

        } catch (e: NetworkException) {
            Log.w("UserRepository", "Network error, trying local cache for user $id", e)
            try {
                val dto = localDataSource.getUserFromAssets("user$id.json")
                mapper.toModel(dto).also { userCache[id] = it }
            } catch (localEx: UserNotFoundException) {
                throw UserNotFoundException(
                    userId = id,
                    message = "Пользователь не найден в сети и локальном кэше",
                    cause = localEx
                )
            } catch (localEx: Exception) {
                Log.e("UserRepository", "Local cache parsing failed for user $id", localEx)
                throw DataException(
                    message = "Ошибка чтения локальных данных для пользователя $id",
                    cause = localEx
                )
            }
        } catch (e: Exception) {
            throw DataException(
                message = "Неожиданная ошибка при получении пользователя $id: ${e.message}",
                cause = e
            )
        }
    }

    override fun clearCache() {
        userCache.clear()
        Log.d("UserRepository", "User cache cleared")
    }
}