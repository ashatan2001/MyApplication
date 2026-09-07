package com.example.data.remote

import com.example.data.dto.LoginRequestDto
import com.example.data.dto.LoginResponseDto
import com.example.data.dto.UserDto
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

    suspend fun getCurrentUser(): UserDto = safeApiCall { api.getCurrentUser() }
}