package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.exception.*
import com.example.domain.usecase.GetUserNameUseCase
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

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val userName: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class HomeEvent {
    data object NavigateToLogin : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserName: GetUserNameUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result = getUserName()) {
                is CustomResult.Success -> {
                    _uiState.value = HomeUiState.Success(userName = result.data)
                }
                is CustomResult.Error -> {
                    result.exception?.let { handleError(it) }
                }
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                logoutUseCase()
            } catch (e: Exception) {
                Timber.w(e, "Ошибка логаута, выполняем локальный выход")
            } finally {
                _events.trySend(HomeEvent.NavigateToLogin)
                onLogout()
            }
        }
    }

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

    private fun Throwable.toUserMessage(): String = when (this) {
        is UserNotFoundException -> "Пользователь не найден"
        is NetworkConnectionException -> "Нет соединения с интернетом"
        is ServerUnavailableException -> "Ошибка сервера, попробуйте позже"
        is ServerApiException -> message ?: "Ошибка API"
        else -> message ?: "Неизвестная ошибка"
    }
}