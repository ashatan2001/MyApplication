package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.exception.NetworkException
import com.example.domain.exception.UserNotFoundException
import com.example.domain.repository.AuthRepository
import com.example.domain.usecase.GetUserUseCase
import com.example.domain.util.Result
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
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadUserData() }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val currentUser = authRepository.getCurrentUser()
                when (val result = getUser(currentUser.id)) {
                    is Result.Success -> {
                        _uiState.value = HomeUiState.Success(
                            userName = result.data.fio,
                            userId = result.data.id
                        )
                    }
                    is Result.Failure -> {
                        val message = when (result.error) {
                            is UserNotFoundException -> "Пользователь не найден"
                            is NetworkException -> "Нет соединения с интернетом"
                            else -> result.error.message ?: "Неизвестная ошибка"
                        }
                        _uiState.value = HomeUiState.Error(message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Ошибка")
            }
        }
    }
}