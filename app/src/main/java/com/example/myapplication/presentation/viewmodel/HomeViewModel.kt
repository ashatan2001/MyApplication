package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.exception.*
import com.example.domain.usecase.GetPersonNameUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Состояние UI для главного экрана.
 *
 * [Loading] — отображается спиннер во время загрузки имени пользователя
 * [Success] — отображается приветствие с именем пользователя
 * [Error] — отображается сообщение об ошибке
 */
sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val personName: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

/**
 * Одноразовые события главного экрана для навигации.
 */
sealed class HomeEvent {
    /** Переход на экран логина при истечении сессии или выходе. */
    data object NavigateToLogin : HomeEvent()
}

/**
 * ViewModel главного экрана.
 *
 * Загружает имя пользователя для приветствия и обрабатывает выход из системы.
 *
 * @param getPersonName UseCase для получения имени пользователя из кэша.
 * @param logoutUseCase UseCase для выполнения выхода из системы.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPersonName: GetPersonNameUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Загружает имя пользователя для приветствия.
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result = getPersonName()) {
                is CustomResult.Success -> {
                    _uiState.value = HomeUiState.Success(personName = result.data)
                }
                is CustomResult.Error -> {
                    result.exception?.let { handleError(it) }
                }
            }
        }
    }

    /**
     * Выполняет выход из системы.
     *
     * При сетевой ошибке всё равно выполняет локальный выход,
     * чтобы пользователь не остался «заперт» в приложении.
     *
     * @param onLogout Колбэк, вызываемый после завершения выхода.
     */
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                logoutUseCase()
            } catch (e: Exception) {
                // Сетевая ошибка не блокирует выход — очистка всё равно выполняется
                Timber.w(e, "Ошибка логаута, выполняем локальный выход")
            } finally {
                _events.trySend(HomeEvent.NavigateToLogin)
                onLogout()
            }
        }
    }

    /**
     * Обрабатывает ошибки загрузки данных.
     *
     * При истечении сессии или невалидной аутентификации
     * перенаправляет пользователя на экран логина.
     */
    private fun handleError(exception: Throwable) {
        when (exception) {
            is SessionExpiredDomainException,
            is AuthenticationFailedException -> {
                Timber.w("Сессия истекла или невалидна, редирект на логин")
                _events.trySend(HomeEvent.NavigateToLogin)
            }
            else -> {
                _uiState.value = HomeUiState.Error(
                    message = exception.toUserMessage()
                )
            }
        }
    }

    /**
     * Маппит исключения в человекочитаемые сообщения для пользователя.
     */
    private fun Throwable.toUserMessage(): String = when (this) {
        is PersonNotFoundException -> "Пользователь не найден"
        is NetworkConnectionException -> "Нет соединения с интернетом"
        is ServerUnavailableException -> "Ошибка сервера, попробуйте позже"
        is ServerApiException -> message ?: "Ошибка API"
        else -> message ?: "Неизвестная ошибка"
    }
}