package com.example.data.repository

import android.util.Log
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.AuthMapper
import com.example.data.mapper.UserMapper
import com.example.data.network.CustomCookieJar
import com.example.data.remote.AuthRemoteDataSource
import com.example.domain.model.AuthState
import com.example.domain.model.AuthSuccess
import com.example.domain.model.UserModel
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val cookieJar: CustomCookieJar,
    private val userRepository: UserRepository,
    private val authMapper: AuthMapper,
    private val userMapper: UserMapper
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthSuccess {
        val dto = remoteDataSource.login(username, password)
        localDataSource.saveAuthState(true) // Сохраняем состояние при успешном входе
        return authMapper.toDomain(dto)
    }

    override suspend fun refreshToken() {
        remoteDataSource.refreshToken()
    }

    override suspend fun logout() {
        try {
            // Пытаемся уведомить сервер о выходе (не критично, если сеть недоступна)
            remoteDataSource.logout()
        } catch (e: Exception) {
            Log.w("AuthRepository", "Network error during logout, proceeding with local cleanup", e)
        } finally {
            // Гарантированная очистка локальных данных в любом сценарии
            cookieJar.clear()
            localDataSource.clearSession() // Это триггерит обновление UI через Flow
            userRepository.clearCache()
        }
    }

    override suspend fun getCurrentUser(): UserModel {
        val dto = remoteDataSource.getCurrentUser()
        return userMapper.toModel(dto)
    }

    override fun getAuthState(): Flow<AuthState> =
        localDataSource.getAuthState().map { isAuthenticated ->
            if (isAuthenticated) AuthState.Authenticated else AuthState.Unauthenticated
        }
}