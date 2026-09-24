package com.example.domain.usecase

import com.example.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use-case для завершения текущей сессии пользователя.
 *
 * Инкапсулирует логику выхода из системы, включая очистку локальных токенов,
 * кук и сброс состояния сессии в репозитории.
 *
 * @param repository Репозиторий аутентификации для выполнения операций выхода.
 */
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Выполняет процедуру выхода из системы.
     *
     * Является suspend-функцией, так как может включать асинхронные операции
     * по очистке сетевых сессий или локального хранилища.
     */
    suspend operator fun invoke() {
        repository.logout()
    }
}