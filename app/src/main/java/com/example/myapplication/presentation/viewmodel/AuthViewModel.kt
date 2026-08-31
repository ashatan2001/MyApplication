package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.domain.model.UserModel
import com.example.domain.usecase.LoginUseCase
import javax.inject.Inject

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data class Success(val user: UserModel) : AuthUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : AuthUiState()
}

class AuthViewModel @Inject constructor(
    private val getLogin: LoginUseCase
) : ViewModel() {


}