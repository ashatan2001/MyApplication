// data/src/main/java/com/example/data/repository/UserRepositoryImpl.kt
package com.example.data.repository

import com.example.data.exception.*
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.UserMapper
import com.example.data.remote.UserRemoteDataSource
import com.example.domain.exception.*
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

        val dto = try {
            remoteDataSource.getUserInfo(userId)
        } catch (e: UnauthorizedException) {
            localDataSource.clearSession()
            throw AuthenticationFailedException(
                message = e.message ?: "Ошибка авторизации",
                cause = e
            )
        } catch (e: SessionExpiredException) {
            localDataSource.clearSession()
            throw SessionExpiredDomainException(
                message = e.message ?: "Сессия истекла",
                cause = e
            )
        } catch (e: NetworkException) {
            throw NetworkConnectionException(
                message = e.message ?: "Нет подключения к интернету",
                cause = e
            )
        } catch (e: ServerException) {
            throw ServerUnavailableException(
                message = e.message ?: "Ошибка сервера",
                cause = e
            )
        } catch (e: ApiException) {
            throw ServerApiException(
                errorNumber = e.errorNumber,
                message = e.message ?: "Ошибка API",
                cause = e
            )
        } catch (e: DataLayerException) {
            if (e.errorCode == 404) {
                throw UserNotFoundException(
                    userId = userId,
                    message = "Пользователь не найден на сервере"
                )
            }
            throw AppException(
                message = "Ошибка сети при получении данных пользователя (код: ${e.errorCode})",
                cause = e
            )
        } catch (e: UserNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw AppException(
                message = "Неожиданная ошибка при получении пользователя $userId: ${e.message ?: "неизвестная ошибка"}",
                cause = e
            )
        }

        return mapper.toModel(dto)
    }
}