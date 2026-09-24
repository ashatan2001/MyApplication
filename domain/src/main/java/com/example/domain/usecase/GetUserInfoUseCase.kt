package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

/**
 * Use-case для получения полной информации о текущем авторизованном пользователе.
 *
 * Инкапсулирует логику запроса профиля из [UserRepository] и оборачивает результат
 * в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы с данными пользователя.
 */
class GetUserInfoUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): CustomResult<UserModel> {
        return try {
            val user = repository.getUserInfo()
            CustomResult.Success(user)
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(AppException(message = "Failed to get current user", cause = e))
        }
    }
}