package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.remote.PersonRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.exception.PersonNotFoundException
import com.example.domain.model.Person
import com.example.domain.repository.PersonRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepositoryImpl @Inject constructor(
    private val remoteDataSource: PersonRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : PersonRepository {

    override suspend fun getPersonId(): Int? = localDataSource.getPersonId()

    override suspend fun getPersonName(): String? = localDataSource.getPersonName()

    override suspend fun getPersonInfo(): Person {
        val personId = getPersonId()
            ?: throw PersonNotFoundException(
                personId = null,
                message = "Пользователь не авторизирован"
            )

        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении данных пользователя",
            onAuthError = { localDataSource.clearSession() },
            notFoundMapper = {
                PersonNotFoundException(
                    personId = personId,
                    message = it.message ?: "Пользователь не найден на сервере"
                )
            }
        ) {
            remoteDataSource.getPersonInfo(personId)
        }
        return dto.toDomain()
    }
}