package com.example.app.presentation.user


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.UserModel
import com.example.domain.exception.NetworkException
import com.example.domain.usecase.UserUseCase
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
class UserViewModel
    @Inject
    constructor(
        private val getUser: UserUseCase
    ) : ViewModel() {

    private val _userState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val userState: StateFlow<UserUiState> = _userState.asStateFlow()

    private var currentUserId: Int? = null

    fun loadUser(userId: Int) {
        if (currentUserId?.equals(userId) == true && _userState.value is UserUiState.Success) {
            return
        }

        currentUserId = userId
        _userState.value = UserUiState.Loading

        viewModelScope.launch {
            try {
                val user = getUser(userId)
                _userState.value = UserUiState.Success(user)
            } catch (e: Exception) {
                _userState.value = UserUiState.Error(
                    message = e.message ?: "Неизвестная ошибка",
                    isNetworkError = e is NetworkException
                )
            }
        }
    }
    }


