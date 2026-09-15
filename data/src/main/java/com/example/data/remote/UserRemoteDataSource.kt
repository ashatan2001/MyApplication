package com.example.data.remote

import com.example.data.dto.UserDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val api: UserApi
) {
    suspend fun getUserInfo(id: Int): UserDto = safeApiCall { api.getUser(id) }
}