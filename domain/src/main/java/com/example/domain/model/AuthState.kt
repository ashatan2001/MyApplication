package com.example.domain.model

/**
 * Состояние авторизации пользователя.
 *
 * Используется [AuthRepository.getAuthState] для реактивного
 * наблюдения из UI и навигационных графов.
 */
sealed class AuthState {

    /** Идёт проверка сохранённой сессии. */
    data object Loading : AuthState()

    /** Пользователь авторизован, доступен основной функционал. */
    data object Authenticated : AuthState()

    /** Пользователь не авторизован, требуется вход. */
    data object Unauthenticated : AuthState()

    /**
     * Ошибка при проверке состояния сессии.
     *
     * @property message Описание ошибки для отображения пользователю.
     */
    data class Error(val message: String) : AuthState()
}