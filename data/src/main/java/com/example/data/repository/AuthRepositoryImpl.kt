package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.AuthMapper
import com.example.data.remote.AuthRemoteDataSource
import com.example.domain.repository.AuthRepository
import com.example.domain.model.SignInModel
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        //private val api: UserApi,
        private val remoteDataSource: AuthRemoteDataSource,
        private val localDataSource: AuthLocalDataSource,
        private val mapper: AuthMapper
    ) : AuthRepository {
        override suspend fun login() {
            TODO("Not yet implemented")
        }

        override suspend fun logout() {
            TODO("Not yet implemented")
        }

        override suspend fun getAuthState() {
            TODO("Not yet implemented")
        }

        override suspend fun getToken() {
            TODO("Not yet implemented")
        }
}