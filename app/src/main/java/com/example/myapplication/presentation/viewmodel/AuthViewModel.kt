package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.exception.NetworkException
import com.example.data.exception.ServerException
import com.example.domain.exception.AuthenticationException
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val fio: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = loginUseCase(username, password)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success(result.data.fullname)
                    _events.trySend(AuthEvent.LoginSuccess)
                    _uiState.value = AuthUiState.Idle
                }
                is Result.Failure -> {
                    val message: String = when (val error = result.error) {
                        is AuthenticationException -> "Неверный логин или пароль"
                        is NetworkException -> "Нет соединения с интернетом"
                        is ServerException -> "Ошибка сервера: ${error.message ?: "неизвестно"}"
                        else -> {
                            android.util.Log.e("AuthViewModel", "Ошибка входа", error)
                            error.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = AuthUiState.Error(message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                logoutUseCase()
            } catch (_: Exception) { }
            _uiState.value = AuthUiState.Idle
        }
    }
}