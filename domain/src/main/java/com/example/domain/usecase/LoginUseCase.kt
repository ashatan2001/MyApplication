package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.ValidationException
import com.example.domain.model.AuthSuccess
import com.example.domain.repository.AuthRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(login: String, password: String): CustomResult<AuthSuccess> {
        println("DEBUG [UseCase] 1. Вызов repository.login...")
        return try {
            validateCredentials(login, password)

            val result = repository.login(login, password)
            println("DEBUG [UseCase] 2. Repository вернул Успех: $result")
            CustomResult.Success(result)
        } catch (e: ValidationException) {
            CustomResult.Error(e)
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            println("DEBUG [UseCase] 3. Repository выбросил ОШИБКУ: ${e::class.simpleName} | ${e.message}")
            CustomResult.Error(AppException(message = "Unexpected error", cause = e))
        }
    }

    private fun validateCredentials(login: String, password: String) {
        if (login.isBlank()) {
            throw ValidationException(field = "login", message = "Логин не может быть пустым")
        }
        if (password.isBlank()) {
            throw ValidationException(field = "password", message = "Пароль не может быть пустым")
        }
        if (login.length < 3) {
            throw ValidationException(field = "login", message = "Логин должен быть не менее 3 символов")
        }
        if (password.length < 6) {
            throw ValidationException(field = "password", message = "Пароль должен быть не менее 6 символов")
        }
    }
}