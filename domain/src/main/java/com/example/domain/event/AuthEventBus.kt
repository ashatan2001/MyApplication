package com.example.domain.event

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Шина событий для обмена сообщениями между слоями приложения.
 * Используется для глобальных уведомлений, таких как принудительный выход из системы
 * при истечении сессии или удалении аккаунта.
 */
interface AuthEventBus {
    /**
     * Поток событий, сигнализирующих о необходимости завершения сессии.
     */
    val logoutEvents: SharedFlow<Unit>

    /**
     * Отправляет событие о необходимости выхода из системы.
     */
    suspend fun notifyLogout()
}

/**
 * Реализация [AuthEventBus] на основе [SharedFlow].
 * Является синглтоном в рамках Dagger Hilt, чтобы гарантировать единую точку
 * подписки для всех компонентов приложения.
 */
@Singleton
class AuthEventBusImpl @Inject constructor() : AuthEventBus {

    private val _logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val logoutEvents: SharedFlow<Unit> = _logoutEvents

    override suspend fun notifyLogout() {
        _logoutEvents.emit(Unit)
    }
}
