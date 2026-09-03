package com.example.domain.model

sealed class AuthState {
    data object Loading : AuthState()           // Проверка состояния
    data object Authenticated : AuthState()     // Авторизован
    data object Unauthenticated : AuthState()   // Не авторизован
    data class Error(val message: String) : AuthState()  // Ошибка проверки
}