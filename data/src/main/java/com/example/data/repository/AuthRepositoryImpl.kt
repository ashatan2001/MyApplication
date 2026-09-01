package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.network.CustomCookieJar
import com.example.data.remote.AuthRemoteDataSource
import com.example.domain.model.AuthSuccess
import com.example.domain.repository.AuthRepository
import com.example.domain.model.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class AuthRepositoryImpl
    @Inject
    constructor(
        //private val api: UserApi,
        private val remoteDataSource: AuthRemoteDataSource,
        private val localDataSource: AuthLocalDataSource,
        private val cookieJar: CustomCookieJar,
    ) : AuthRepository {

        override suspend fun login(username: String, password: String): AuthSuccess {
            val responseDto = remoteDataSource.login(username, password)
            localDataSource.saveAuthState(true)
            return AuthSuccess(
                fio = responseDto.fio,
                message = responseDto.message
            )
        }

        override suspend fun refreshToken() {
            remoteDataSource.refreshToken()
        }

        override suspend fun logout() {
            remoteDataSource.logout()
            cookieJar.clear()
            localDataSource.clearSession()
        }

        override fun getAuthState(): Flow<AuthState> {
            return localDataSource.getAuthState().map { isAuthenticated ->
                if (isAuthenticated) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
}