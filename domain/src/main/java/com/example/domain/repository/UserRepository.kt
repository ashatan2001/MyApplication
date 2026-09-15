package com.example.domain.repository

import com.example.domain.model.UserModel

interface UserRepository {
    // suspend fun getUserFromJson(id: Int): UserModel
    suspend fun getUserInfo(): UserModel
    suspend fun getUserId(): Int?
    suspend fun getUserName(): String?
    fun clearCache()
}