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
import javax.inject.Inject


sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val userName: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object logoutSuccess: AuthEvent()
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
            android.util.Log.d("LOGIN_DEBUG", "1. Начало загрузки")
            _uiState.value = AuthUiState.Loading

            try {
                android.util.Log.d("LOGIN_DEBUG", "2. Вызов loginUseCase...")
                val result = loginUseCase(username, password)
                android.util.Log.d("LOGIN_DEBUG", "3. UseCase вернул результат: ${result::class.simpleName}")

                when (result) {
                    is CustomResult.Success -> {
                        android.util.Log.d("LOGIN_DEBUG", "4. Успех: ${result.data.userName}")
                        _uiState.value = AuthUiState.Success(result.data.userName)
                        _events.trySend(AuthEvent.LoginSuccess)
                    }
                    is CustomResult.Error -> {
                        val e = result.exception
                        android.util.Log.e("LOGIN_DEBUG", "4. Ошибка! Тип: ${e?.javaClass?.simpleName}, Сообщение: ${e?.message}", e)

                        val message: String = when (e) {
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
                android.util.Log.e("LOGIN_DEBUG", "5. НЕОБРАБОТАННЫЙ КРАШ В VIEWMODEL", t)
                _uiState.value = AuthUiState.Error(t.message ?: "Критическая ошибка")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                logoutUseCase()
                _events.trySend(AuthEvent.logoutSuccess)
            } catch (e: Exception) {
                android.util.Log.w("AuthViewModel", "Ошибка сетевого логаута, выполняем локальный выход", e)
                _events.trySend(AuthEvent.logoutSuccess)
            } finally {
                _uiState.value = AuthUiState.Idle
            }
        }
    }
}