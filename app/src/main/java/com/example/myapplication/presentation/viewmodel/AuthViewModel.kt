package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.exception.ApiException
import com.example.data.exception.NetworkException
import com.example.data.exception.ServerException
import com.example.data.exception.UnauthorizedException
import com.example.domain.usecase.LoginUseCase
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
 * Представляет состояние экрана авторизации.
 */
sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val userName: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * Одноразовые события экрана авторизации для навигации или показа Snackbar.
 */
sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object LogoutSuccess : AuthEvent()
}

/**
 * ViewModel для управления состоянием и бизнес-логикой экрана авторизации.
 *
 * @param loginUseCase UseCase для выполнения входа в систему.
 * @param logoutUseCase UseCase для выполнения выхода из системы.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Инициирует процесс авторизации пользователя.
     *
     * @param username Имя пользователя.
     * @param password Пароль пользователя.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            Timber.d("Начало процесса авторизации для пользователя: $username")
            _uiState.value = AuthUiState.Loading

            try {
                val result = loginUseCase(username, password)
                Timber.d("UseCase вернул результат: ${result::class.simpleName}")

                when (result) {
                    is CustomResult.Success -> {
                        Timber.d("Авторизация успешна: ${result.data.userName}")
                        _uiState.value = AuthUiState.Success(result.data.userName)
                        _events.trySend(AuthEvent.LoginSuccess)
                    }
                    is CustomResult.Error -> {
                        val e = result.exception
                        Timber.e(e, "Ошибка авторизации. Тип: ${e?.javaClass?.simpleName}")

                        val message = when (e) {
                            is UnauthorizedException -> e.message ?: "Сессия истекла. Пожалуйста, войдите снова."
                            is ApiException -> e.message ?: "Ошибка авторизации"
                            is NetworkException -> "Нет подключения к интернету"
                            is ServerException -> "Ошибка сервера, попробуйте позже"
                            else -> e?.message ?: "Неизвестная ошибка"
                        }
                        _uiState.value = AuthUiState.Error(message)
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t, "Необработанный сбой в AuthViewModel")
                _uiState.value = AuthUiState.Error(t.message ?: "Критическая ошибка")
            }
        }
    }

    /**
     * Инициирует процесс выхода пользователя из системы.
     */
    fun logout() {
        viewModelScope.launch {
            Timber.d("Начало процесса выхода из системы")
            _uiState.value = AuthUiState.Loading
            try {
                logoutUseCase()
                _events.trySend(AuthEvent.LogoutSuccess)
            } catch (e: Exception) {
                Timber.w(e, "Ошибка сетевого логаута, выполняем локальный выход")
                _events.trySend(AuthEvent.LogoutSuccess)
            } finally {
                _uiState.value = AuthUiState.Idle
            }
        }
    }
}