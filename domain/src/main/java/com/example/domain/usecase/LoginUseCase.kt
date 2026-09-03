package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.AuthenticationException
import com.example.domain.exception.ValidationException
import com.example.domain.model.AuthSuccess
import com.example.domain.repository.AuthRepository
import com.example.domain.util.Result
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(login: String, password: String): Result<AuthSuccess> {
        return try {
            validateCredentials(login, password)

            val result = repository.login(login, password)
            Result.Success(result)

        } catch (e: AuthenticationException) {
            Result.Failure(e)
        } catch (e: ValidationException) {
            Result.Failure(e)
        } catch (e: AppException) {
            Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(AppException(message = "Unexpected error", cause = e))
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