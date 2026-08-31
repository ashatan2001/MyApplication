package com.example.data.remote

import com.example.data.dto.LoginRequestDto
import com.example.data.dto.LoginResponseDto
import com.example.data.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("sign-in")
    suspend fun login(body: LoginRequestDto): Response<LoginResponseDto>

    @GET("get-token")
    suspend fun refreshToken(): Response<Unit>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @GET("profile")
    suspend fun getCurrentUser(): Response<UserDto>
}