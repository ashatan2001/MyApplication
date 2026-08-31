package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login()
    suspend fun logout()
    suspend fun getToken()
    suspend fun getAuthState(): Flow<AuthState>
}