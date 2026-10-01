package com.example.data.repository

import com.example.data.exception.*
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.PersonMapper
import com.example.data.remote.PersonRemoteDataSource
import com.example.domain.exception.*
import com.example.domain.model.Person
import com.example.domain.repository.PersonRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepositoryImpl @Inject constructor(
    private val remoteDataSource: PersonRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val mapper: PersonMapper
) : PersonRepository {

    override suspend fun getPersonId(): Int? = localDataSource.getPersonId()

    override suspend fun getPersonName(): String? = localDataSource.getPersonName()

    override suspend fun getPersonInfo(): Person {
        val personId = getPersonId()
            ?: throw PersonNotFoundException(personId = null, message = "Пользователь не авторизирован")

        val dto = try {
            remoteDataSource.getPersonInfo(personId)
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
                throw PersonNotFoundException(
                    personId = personId,
                    message = e.message ?: "Пользователь не найден на сервере"
                )
            }
            throw AppException(
                message = "Ошибка сети при получении данных пользователя (код: ${e.errorCode})",
                cause = e
            )
        } catch (e: PersonNotFoundException) {
            // Уже доменное исключение — пробрасываем без повторной обёртки
            throw e
        } catch (e: Exception) {
            throw AppException(
                message = "Неожиданная ошибка при получении пользователя $personId: ${e.message ?: "неизвестная ошибка"}",
                cause = e
            )
        }

        return mapper.toModel(dto)
    }
}