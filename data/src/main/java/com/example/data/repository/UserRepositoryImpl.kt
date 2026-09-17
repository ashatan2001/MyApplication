package com.example.data.repository

import com.example.data.exception.DataException
import com.example.data.exception.DataLayerException
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.UserMapper
import com.example.data.remote.UserRemoteDataSource
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val mapper: UserMapper
) : UserRepository {

    override suspend fun getUserId(): Int? = localDataSource.getUserId()

    override suspend fun getUserName(): String? = localDataSource.getUserName()

    override suspend fun getUserInfo(): UserModel {
        val userId = getUserId()
            ?: throw UserNotFoundException(userId = null, message = "Пользователь не авторизирован")

        return try {
            val dto = remoteDataSource.getUserInfo(userId)

            mapper.toModel(dto)

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
}