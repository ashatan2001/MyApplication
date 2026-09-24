package com.example.domain.repository

import com.example.domain.model.AuthState
import com.example.domain.model.AuthSuccess
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для управления процессами аутентификации и сессией пользователя.
 *
 * Реализация должна гарантировать потокобезопасность и корректную очистку
 * локальных данных при выходе из системы.
 */
interface AuthRepository {

    /**
     * Выполняет попытку входа в систему с указанными учетными данными.
     *
     * При успехе сохраняет токены/куки в локальное хранилище
     * и эмитирует [AuthState.Authenticated] в [getAuthState].
     *
     * @param username Логин пользователя.
     * @param password Пароль пользователя.
     * @return [AuthSuccess] с информацией об успешной авторизации.
     * @throws Exception при сетевых ошибках, ошибках сервера или неверных учетных данных.
     */
    suspend fun login(username: String, password: String): AuthSuccess

    /**
     * Обновляет токены доступа (refresh token) для продления сессии.
     *
     * Вызывается автоматически при получении 401/419 от сервера.
     * При успехе обновляет локальные токены.
     * При ошибке очищает локальную сессию и эмитирует [AuthState.Unauthenticated].
     *
     * @throws Exception если обновление токена невозможно (токен отозван, сеть недоступна).
     */
    suspend fun refreshToken()

    /**
     * Завершает текущую сессию пользователя.
     *
     * Выполняет сетевой запрос на инвалидацию токена (если возможно),
     * затем **всегда** очищает локальные токены/куки, даже если сетевой запрос не удался.
     * После очистки эмитирует [AuthState.Unauthenticated] в [getAuthState].
     *
     * @throws Exception только при критических ошибках локального хранилища.
     *         Сетевые ошибки не пробрасываются.
     */
    suspend fun logout()

    /**
     * Предоставляет реактивный поток состояний авторизации.
     *
     * Эмитит [AuthState] при изменении статуса сессии:
     * - [AuthState.Loading] — начальное состояние при запуске
     * - [AuthState.Authenticated] — пользователь авторизован
     * - [AuthState.Unauthenticated] — пользователь не авторизован
     *
     * @return [Flow] состояний авторизации. Не завершается (не вызывает [onComplete]).
     */
    fun getAuthState(): Flow<AuthState>
}