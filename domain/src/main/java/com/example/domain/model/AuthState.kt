package com.example.domain.model

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
}