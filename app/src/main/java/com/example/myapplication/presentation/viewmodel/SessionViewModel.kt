package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.model.AuthState
import com.example.domain.usecase.LogoutUseCase
import com.example.domain.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel для управления глобальным состоянием сессии пользователя.
 *
 * Предоставляет реактивный поток состояния авторизации и событий выхода.
 * Используется в [MainActivity] для навигации и показа Snackbar.
 *
 * @param observeAuthStateUseCase UseCase для наблюдения за изменениями состояния авторизации.
 * @param authEventBus Шина событий для получения уведомлений о выходе из системы.
 * @param logoutUseCase UseCase для выполнения выхода (очистка локальных данных и запрос к серверу).
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val authEventBus: AuthEventBus,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    init {
        Timber.d("SessionViewModel инициализирован")
    }

    /**
     * Состояние авторизации пользователя.
     *
     * Использует [SharingStarted.Lazily] для оптимизации ресурсов:
     * подписка на источник создаётся только при появлении первого коллектора.
     */
    val authState: StateFlow<AuthState> = observeAuthStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AuthState.Loading
        )

    /**
     * Поток событий выхода из системы.
     *
     * Используется для навигации на экран авторизации при завершении сессии
     * и показа информационного сообщения (Snackbar) в [MainActivity].
     */
    val logoutEvents: SharedFlow<Unit> = authEventBus.logoutEvents

    /**
     * Инициирует процесс выхода из системы.
     *
     * Выполняет UseCase, который должен:
     * 1. Отправить запрос на отзыв токена на сервер (если есть сеть).
     * 2. Очистить локальное хранилище (токены, данные пользователя).
     * 3. Отправить событие в [AuthEventBus] или обновить состояние через [ObserveAuthStateUseCase].
     *
     * В случае сетевой ошибки локальная очистка всё равно должна произойти,
     * чтобы пользователь не оказался в заблокированном состоянии.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                Timber.d("Выход из системы выполнен успешно")
            } catch (e: Exception) {
                Timber.w(e, "Сетевой запрос на logout не удался, но локальный выход должен быть выполнен")
            }
        }
    }
}