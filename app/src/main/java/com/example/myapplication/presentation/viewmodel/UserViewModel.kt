package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.exception.NetworkException
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.usecase.GetUserInfoUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserUiState {
    data object Loading : UserUiState()
    data class Success(val user: UserModel) : UserUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : UserUiState()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading

            when (val result = getUserInfoUseCase()) {
                is CustomResult.Success -> {
                    val user = result.data
                    _uiState.value = UserUiState.Success(user)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is UserNotFoundException -> "Пользователь не найден"
                        is NetworkException -> "Нет соединения с интернетом"
                        else -> result.exception.message ?: "Неизвестная ошибка"
                    }
                    _uiState.value = UserUiState.Error(message)
                }
            }
        }
    }
}