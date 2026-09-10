package com.example.myapplication.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.exception.NetworkException
import com.example.domain.exception.UserNotFoundException
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.GetUserUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val userName: String, val userId: Int) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUser: GetUserUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadUserData() }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // 1. Получаем текущего пользователя через use case
            when (val currentUserResult = getCurrentUser()) {
                is CustomResult.Error -> {
                    _uiState.value = HomeUiState.Error(
                        currentUserResult.exception.message ?: "Не удалось получить пользователя"
                    )
                    return@launch
                }
                is CustomResult.Success -> {
                    // 2. Загружаем полные данные пользователя
                    when (val result = getUser(currentUserResult.data.id)) {
                        is CustomResult.Success -> {
                            _uiState.value = HomeUiState.Success(
                                userName = result.data.fullname,
                                userId = result.data.id
                            )
                        }
                        is CustomResult.Error -> {
                            val message: String = when (val error = result.exception) {
                                is UserNotFoundException -> "Пользователь не найден"
                                is NetworkException -> "Нет соединения с интернетом"
                                else -> result.exception.message ?: "Неизвестная ошибка"
                            }
                            _uiState.value = HomeUiState.Error(message)
                        }
                    }
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                logoutUseCase()
                onSuccess()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Logout failed", e)
                // Даже при ошибке считаем пользователя разлогиненным
                onSuccess()
            }
        }
    }
}