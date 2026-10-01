package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.Person
import com.example.domain.repository.PersonRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

/**
 * Use-case для получения полной информации о текущем авторизованном пользователе.
 *
 * Инкапсулирует логику запроса профиля из [PersonRepository] и оборачивает результат
 * в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы с данными пользователя.
 */
class GetPersonInfoUseCase @Inject constructor(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(): CustomResult<Person> {
        return try {
            val user = repository.getPersonInfo()
            CustomResult.Success(user)
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(AppException(message = "Failed to get current user", cause = e))
        }
    }
}