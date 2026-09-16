package com.example.data.repository

import android.util.Log
import com.example.data.exception.DataException
import com.example.data.exception.DataLayerException
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.UserMapper
import com.example.data.network.AuthEventBus
import com.example.data.remote.UserRemoteDataSource
import com.example.data.util.safeApiCallWithRetry
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UserRepository
import dagger.internal.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val mapper: UserMapper,
    private val authEventBus: AuthEventBus,
    private val authRepositoryProvider: Provider<AuthRepository>
) : UserRepository {

    private val userCache = ConcurrentHashMap<Int, UserModel>()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            authEventBus.logoutEvents.collect {
                clearCache()
            }
        }
    }

    override suspend fun getUserId(): Int? = localDataSource.getUserId()

    override suspend fun getUserName(): String? = localDataSource.getUserName()

    override suspend fun getUserInfo(): UserModel {
        val userId = getUserId()
            ?: throw UserNotFoundException(userId = null, message = "Пользователь не авторизирован")

        userCache[userId]?.let { return it }

        return try {
            val dto = safeApiCallWithRetry(
                block = { remoteDataSource.getUserInfo(userId) },
                onRefreshToken = { authRepositoryProvider.get().tryRefreshToken() }
            )

            mapper.toModel(dto).also { userCache[userId] = it }
        } catch (e: DataLayerException) {
            if (e.errorCode == 404) {
                throw UserNotFoundException(
                    userId = userId,
                    message = "Пользователь не найден на сервере"
                )
            }
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