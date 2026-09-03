package com.example.domain.repository

import com.example.domain.model.AuthState
import com.example.domain.model.AuthSuccess
import com.example.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthSuccess
    suspend fun refreshToken()
    suspend fun logout()
    suspend fun getCurrentUser(): UserModel
    fun getAuthState(): Flow<AuthState>
}