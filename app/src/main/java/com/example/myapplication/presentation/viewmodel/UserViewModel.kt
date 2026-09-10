package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.exception.NetworkException
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.usecase.GetUserUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    private val _userState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val userState: StateFlow<UserUiState> = _userState.asStateFlow()

    private var currentUserId: Int? = null
    private var loadJob: Job? = null

    fun loadUser(userId: Int) {
        if (currentUserId == userId && _userState.value is UserUiState.Success) return
        currentUserId = userId
        doLoad(userId)
    }

    fun retry() {
        currentUserId?.let { doLoad(it) }
    }

    private fun doLoad(userId: Int) {
        _userState.value = UserUiState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = getUserUseCase(userId)) {
                is CustomResult.Success -> _userState.value = UserUiState.Success(result.data)
                is CustomResult.Error -> {
                    val message = when (val error = result.exception) {
                        is UserNotFoundException -> "Пользователь не найден"
                        is NetworkException -> "Нет соединения с интернетом"
                        else -> result.exception.message ?: "Неизвестная ошибка"
                    }
                    _userState.value = UserUiState.Error(
                        message = message,
                        isNetworkError = result.exception is NetworkException
                    )
                }
            }
        }
    }
}