package com.example.data.remote

import android.util.Log
import com.example.data.dto.LoginRequestDto
import com.example.data.dto.LoginResponseDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi
) {
    suspend fun login(login: String, password: String): LoginResponseDto {
        val requestDto = LoginRequestDto(username = login, password = password)
        return safeApiCall { api.login(requestDto) }
    }

    suspend fun refreshToken() {
        safeApiCall<Unit> { api.refreshToken() }
    }

    suspend fun logout() {
        safeApiCall<Unit> { api.logout() }
    }
    suspend fun tryRefreshToken(): Boolean {
        return try {
            safeApiCall<Unit> { api.refreshToken() }
            true
        } catch (e: Exception) {
            Log.w("AuthRemoteDataSource", "Refresh token failed", e)
            false
        }
    }
}