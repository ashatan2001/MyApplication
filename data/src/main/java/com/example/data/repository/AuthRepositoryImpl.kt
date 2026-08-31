package com.example.data.repository

import com.example.data.dto.LoginResponseDto
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.AuthMapper
import com.example.data.network.CustomCookieJar
import com.example.data.remote.AuthRemoteDataSource
import com.example.domain.model.LoginResponseModel
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        //private val api: UserApi,
        private val remoteDataSource: AuthRemoteDataSource,
        private val localDataSource: AuthLocalDataSource,
        private val cookieJar: CustomCookieJar,
        private val mapper: AuthMapper
    ) : AuthRepository {
        override suspend fun login(username: String, password: String): LoginResponseModel {
            val responseDto = remoteDataSource.login(username, password)
            localDataSource.saveAuthState(true)
            return mapper.toModel(responseDto)
        }

        override suspend fun refreshToken() {
            remoteDataSource.refreshToken()
        }

        override suspend fun logout() {
            remoteDataSource.logout()
            cookieJar.clear()
            localDataSource.clearSession()
        }

        override suspend fun getAuthState() {
            TODO("Not yet implemented")
        }


}