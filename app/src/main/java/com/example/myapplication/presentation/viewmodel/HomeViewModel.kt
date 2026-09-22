package com.example.myapplication.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.exception.NetworkException
import com.example.data.exception.SessionExpiredException
import com.example.data.exception.UnauthorizedException
import com.example.domain.exception.UserNotFoundException
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
import javax.inject.Inject

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val userName: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
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
                    if (result.exception is SessionExpiredException) {
                        return@launch // Тихо игнорируем, редирект уже идет
                    }

                    val message = when (result.exception) {
                        is UserNotFoundException -> "Пользователь не найден"
                        is NetworkException -> "Нет соединения с интернетом"
                        else -> result.exception.message ?: "Неизвестная ошибка"
                    }
                    _uiState.value = HomeUiState.Error(
                        result.exception.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                logoutUseCase()
                // Успешный выход
                _events.trySend(HomeEvent.NavigateToLogin)
                onLogout()
            } catch (e: SessionExpiredException) {
                Log.w("HomeViewModel", "Сессия истекла: ${e.message}", e)
                _events.trySend(HomeEvent.NavigateToLogin)
                onLogout()
            } catch (e: UnauthorizedException) {
                    // 419 или 401 ошибка - сессия истекла
                    Log.w("HomeViewModel", "Сессия истекла: ${e.message}", e)
                    _events.trySend(HomeEvent.NavigateToLogin)
                    onLogout()
            } catch (e: Exception) {
                // Другие ошибки - всё равно очищаем локально
                Log.w("HomeViewModel", "Ошибка логаута: ${e.message}", e)
                _events.trySend(HomeEvent.NavigateToLogin)
                onLogout()
            }
        }
    }
}

sealed class HomeEvent {
    data object NavigateToLogin : HomeEvent()
}
