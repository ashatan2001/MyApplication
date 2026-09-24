package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.model.AuthState
import com.example.domain.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel для управления глобальным состоянием сессии.
 * Предоставляет реактивный поток состояния авторизации и событий выхода.
 *
 * @param observeAuthStateUseCase UseCase для наблюдения за изменениями состояния авторизации.
 * @param authEventBus Шина событий для получения уведомлений о выходе из системы.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    init {
        Timber.d("SessionViewModel инициализирован")
    }

    /**
     * Состояние авторизации пользователя.
     * Использует [SharingStarted.Lazily] для оптимизации ресурсов.
     */
    val authState: StateFlow<AuthState> = observeAuthStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AuthState.Loading
        )

    /**
     * Поток событий выхода из системы.
     * Используется для навигации на экран авторизации при завершении сессии.
     */
    val logoutEvents: SharedFlow<Unit> = authEventBus.logoutEvents
}