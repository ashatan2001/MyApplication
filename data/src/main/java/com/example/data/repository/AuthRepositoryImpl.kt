package com.example.data.repository

import android.util.Log
import com.example.data.exception.*
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.AuthMapper
import com.example.data.network.CustomCookieJar
import com.example.data.remote.AuthRemoteDataSource
import com.example.data.util.JwtParser
import com.example.domain.exception.*
import com.example.domain.model.AuthState
import com.example.domain.model.AuthSuccess
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val cookieJar: CustomCookieJar,
    private val authMapper: AuthMapper,
    private val jwtParser: JwtParser
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthSuccess {
        val dto = try {
            remoteDataSource.login(username, password)
        } catch (e: NetworkException) {
            throw NetworkConnectionException(message = e.message ?: "Нет подключения к интернету", cause = e)
        } catch (e: ServerException) {
            throw ServerUnavailableException(message = e.message ?: "Ошибка сервера", cause = e)
        } catch (e: ApiException) {
            throw ServerApiException(errorNumber = e.errorNumber, message = e.message ?: "Ошибка API", cause = e)
        } catch (e: UnauthorizedException) {
            throw AuthenticationFailedException(message = e.message ?: "Неверные учетные данные", cause = e)
        } catch (e: SessionExpiredException) {
            throw SessionExpiredDomainException(message = e.message ?: "Сессия истекла", cause = e)
        } catch (e: DataLayerException) {
            throw AppException(message = e.message ?: "Неизвестная ошибка", cause = e)
        }

        val token = cookieJar.getAccessToken()

        // Парсим токен и сохраняем userId
        token?.let { jwt ->
            val userId = jwtParser.getUserIdFromToken(jwt)
            if (userId != null) {
                localDataSource.saveUserId(userId)
                Timber.d("UserId $userId извлечен из JWT и сохранен")
            } else {
                Timber.w("Не удалось извлечь userId из токена")
            }
        }

        val userName = dto.userName
        localDataSource.saveUserName(userName)
        Timber.d("FIO $userName сохранено")
        localDataSource.saveAuthState(true)
        return authMapper.toDomain(dto)
    }

    override suspend fun refreshToken() {
        remoteDataSource.refreshToken()
    }

    override suspend fun logout() {
        try {
            remoteDataSource.logout()
        } catch (e: Exception) {
            Timber.w(e, "Network error during logout, proceeding with local cleanup")
        } finally {
            cookieJar.clear()
            localDataSource.clearSession()
            Timber.d("Session fully cleared")
        }
    }

    override fun getAuthState(): Flow<AuthState> =
        localDataSource.getAuthState().map { isAuthenticated ->
            if (isAuthenticated) AuthState.Authenticated else AuthState.Unauthenticated
        }

    private suspend fun clearSession() {
        cookieJar.clear()
        localDataSource.clearSession()
        Timber.d("Session fully cleared")
    }
}