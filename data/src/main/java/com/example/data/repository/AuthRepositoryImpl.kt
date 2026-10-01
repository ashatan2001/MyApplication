package com.example.data.repository

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

/**
 * Реализация [AuthRepository], объединяющая удалённый и локальный источники данных.
 *
 * Отвечает за:
 * - Выполнение входа и маппинг инфраструктурных ошибок в доменные.
 * - Извлечение и сохранение данных пользователя из JWT-токена.
 * - Управление сессией (сохранение/очистка состояния авторизации).
 * - Гарантированную очистку локальных данных при выходе даже при ошибке сети.
 */
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val cookieJar: CustomCookieJar,
    private val authMapper: AuthMapper,
    private val jwtParser: JwtParser
) : AuthRepository {

    /**
     * Выполняет вход и сохраняет данные сессии локально.
     *
     * После успешного входа:
     * 1. Извлекает userId из JWT-токена и сохраняет в локальное хранилище.
     * 2. Сохраняет имя пользователя для отображения в UI.
     * 3. Устанавливает флаг авторизации.
     *
     * @throws NetworkConnectionException при отсутствии сети.
     * @throws AuthenticationFailedException при неверных учётных данных.
     * @throws ServerApiException при бизнес-ошибке сервера.
     */
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

        // Извлекаем userId из JWT и сохраняем для последующих запросов
        extractAndSaveUserId()

        // Сохраняем данные пользователя для отображения без повторного запроса
        val fullName = dto.fullName
        localDataSource.saveUserName(fullName)
        localDataSource.saveAuthState(true)

        Timber.d("Login successful for user: $fullName")
        return authMapper.toDomain(dto)
    }

    /**
     * Обновляет токен сессии.
     * Вызывается [TokenInterceptor] при получении 401/419.
     */
    override suspend fun refreshToken() {
        remoteDataSource.refreshToken()
    }

    /**
     * Завершает сессию пользователя.
     *
     * Пытается инвалидировать сессию на сервере. При сетевой ошибке
     * всё равно очищает локальные данные, гарантируя выход.
     */
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

    /**
     * Реактивный поток состояния авторизации.
     * Маппит булево значение из локального хранилища в [AuthState].
     */
    override fun getAuthState(): Flow<AuthState> =
        localDataSource.getAuthState().map { isAuthenticated ->
            if (isAuthenticated) AuthState.Authenticated else AuthState.Unauthenticated
        }

    // region Private

    /**
     * Извлекает userId из JWT-токена и сохраняет в локальное хранилище.
     * При отсутствии токена или ошибке парсинга — логирует и пропускает.
     */
    private suspend fun extractAndSaveUserId() {
        val token = cookieJar.getAccessToken()
        token?.let { jwt ->
            val userId = jwtParser.getUserIdFromToken(jwt)
            if (userId != null) {
                localDataSource.saveUserId(userId)
                Timber.d("UserId $userId extracted from JWT")
            } else {
                Timber.w("Failed to extract userId from JWT")
            }
        } ?: Timber.w("Access token not found after login")
    }

    /**
     * Полная очистка сессии: куки, локальные данные, состояние авторизации.
     * Вызывается при выходе и при критических ошибках аутентификации.
     */
    private suspend fun clearSession() {
        cookieJar.clear()
        localDataSource.clearSession()
        Timber.d("Session fully cleared")
    }

    // endregion
}