package com.example.domain.repository

import com.example.domain.model.LoginResponseModel
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): LoginResponseModel)
    suspend fun logout()
    suspend fun getToken()
    suspend fun getAuthState(): Flow<AuthState>
}