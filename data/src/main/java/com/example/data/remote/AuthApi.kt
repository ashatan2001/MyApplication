package com.example.data.remote

import com.example.data.dto.LoginRequestDto
import com.example.data.dto.LoginResponseDto
import com.example.data.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/sign-in")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @GET("auth/get-token")
    suspend fun refreshToken(): Response<Unit>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("auth/profile")
    suspend fun getCurrentUser(): Response<UserDto>
}