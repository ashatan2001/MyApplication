package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.presentation.user.UserUiState
import com.example.domain.exception.NetworkException
import com.example.domain.model.UserModel
import com.example.domain.usecase.CheckAuthStateUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val fio: String, val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val checkAuthStateUseCase: CheckAuthStateUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Логин и пароль не могут быть пустыми")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = loginUseCase(username, password)
                _uiState.value = AuthUiState.Success(result.fio, result.message)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Ошибка авторизации")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                logoutUseCase()
                // После успешного логаута сбрасываем состояние UI
                _uiState.value = AuthUiState.Idle
                // И инициируем событие для навигации (например, возврат на экран логина или очистка стека)
                _logoutEvent.emit(Unit)
            } catch (e: Exception) {
                // Даже если сервер вернул ошибку при логауте, мы ВСЕГДА должны
                // очистить локальное состояние и разлогинить пользователя.
                _uiState.value = AuthUiState.Idle
                _logoutEvent.emit(Unit)
            }
        }
    }

    fun checkAuthState() {
        viewModelScope.launch {
            checkAuthStateUseCase().collect { authState -> }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}