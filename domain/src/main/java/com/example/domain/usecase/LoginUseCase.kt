package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.ValidationException
import com.example.domain.model.AuthSuccess
import com.example.domain.repository.AuthRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

/**
 * Use-case для выполнения авторизации пользователя.
 *
 * Инкапсулирует бизнес-логику валидации и аутентификации.
 * Возвращает результат в виде обертки [CustomResult], что позволяет явно обрабатывать
 * успешные сценарии и ошибки, изолируя UI-слой от сетевых исключений.
 *
 * @param repository Реализация [AuthRepository] для выполнения операций авторизации.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    private companion object {
        const val MIN_LOGIN_LENGTH = 3
        const val MIN_PASSWORD_LENGTH = 6
    }

    /**
     * Выполняет попытку входа с указанными учетными данными.
     *
     * @param login Логин пользователя.
     * @param password Пароль пользователя.
     * @return [CustomResult] содержащий [AuthSuccess] или обернутую ошибку.
     */
    suspend operator fun invoke(login: String, password: String): CustomResult<AuthSuccess> {
        return try {
            validateCredentials(login, password)
            val result = repository.login(login, password)
            CustomResult.Success(result)

        } catch (e: ValidationException) {
            // Ошибки валидации пробрасываются как ожидаемые ошибки бизнес-логики
            CustomResult.Error(e)
        } catch (e: AppException) {
            // Ожидаемые инфраструктурные ошибки (сеть, сервер) из слоя данных
            CustomResult.Error(e)
        } catch (e: Exception) {
            // Перехват непредвиденных сбоев для предотвращения краша приложения
            CustomResult.Error(AppException(message = "Unexpected error", cause = e))
        }
    }

    /**
     * Проверяет корректность введенных учетных данных.
     *
     * @throws ValidationException если логин или пароль не соответствуют требованиям безопасности.
     */
    private fun validateCredentials(login: String, password: String) {
        if (login.isBlank()) throw ValidationException("login", "Логин не может быть пустым")
        if (password.isBlank()) throw ValidationException("password", "Пароль не может быть пустым")

        if (login.length < MIN_LOGIN_LENGTH) {
            throw ValidationException("login", "Логин должен быть не менее $MIN_LOGIN_LENGTH символов")
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw ValidationException("password", "Пароль должен быть не менее $MIN_PASSWORD_LENGTH символов")
        }
    }
}