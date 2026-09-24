package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.repository.UserRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

/**
 * Use-case для получения имени текущего авторизованного пользователя.
 *
 * Предоставляет упрощенный доступ только к имени пользователя, скрывая остальную
 * информацию профиля. Обрабатывает случаи, когда имя отсутствует или пользователь не авторизован.
 *
 * @param repository Репозиторий для работы с данными пользователя.
 */
class GetUserNameUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): CustomResult<String> {
        return try {
            val userName = repository.getUserName()
                ?: throw IllegalStateException("Имя пользователя не найдено")

            CustomResult.Success(userName)

        } catch (e: IllegalStateException) {
            CustomResult.Error(
                AppException(
                    message = e.message ?: "Пользователь не авторизирован",
                    cause = e
                )
            )
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка получения данных пользователя: ${e.message}",
                    cause = e
                )
            )
        }
    }
}