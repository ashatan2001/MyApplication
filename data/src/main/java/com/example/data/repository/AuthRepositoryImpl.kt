package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.network.CustomCookieJar
import com.example.data.remote.AuthRemoteDataSource
import com.example.data.util.JwtParser
import com.example.data.util.safeRepositoryCall
import com.example.domain.exception.AuthenticationFailedException
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.ServerApiException
import com.example.domain.model.AuthState
import com.example.domain.model.AuthSuccess
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Реализация [AuthRepository], объединяющая удалённый и локальный источники данных.
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
    private val jwtParser: JwtParser
) : AuthRepository {

    /**
     * Выполняет вход и сохраняет данные сессии локально.
     * После успешного входа:
     * - Извлекает userId из JWT-токена и сохраняет в локальное хранилище.
     * - Сохраняет имя пользователя для отображения в UI.
     * - Устанавливает флаг авторизации.
     *
     * @throws NetworkConnectionException при отсутствии сети.
     * @throws AuthenticationFailedException при неверных учётных данных.
     * @throws ServerApiException при бизнес-ошибке сервера.
     */
    override suspend fun login(username: String, password: String): AuthSuccess {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при входе в систему",
            onAuthError = {
                // При ошибке авторизации очищаем локальные данные
                cookieJar.clear()
                localDataSource.clearSession()
                Timber.w("Ошибка авторизации при входе, сессия очищена")
            }
        ) {
            remoteDataSource.login(username, password)
        }

        // Извлекаем userId из JWT и сохраняем для последующих запросов
        extractAndSaveUserId()

        // Сохраняем данные пользователя для отображения без повторного запроса
        val fullName = dto.fullName
        localDataSource.savePersonName(fullName)
        localDataSource.saveAuthState(true)
        Timber.d("Вход выполнен успешно для пользователя: $fullName")

        return dto.toDomain()
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
     * Пытается инвалидировать сессию на сервере. При сетевой ошибке
     * всё равно очищает локальные данные, гарантируя выход.
     */
    override suspend fun logout() {
        try {
            remoteDataSource.logout()
        } catch (e: Exception) {
            Timber.w(e, "Сетевая ошибка при выходе, выполняем локальную очистку")
        } finally {
            cookieJar.clear()
            localDataSource.clearSession()
            Timber.d("Сессия полностью очищена")
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
                localDataSource.savePersonId(userId)
                Timber.d("UserId $userId извлечён из JWT")
            } else {
                Timber.w("Не удалось извлечь userId из JWT")
            }
        } ?: Timber.w("Access-токен не найден после входа")
    }

    // endregion
}