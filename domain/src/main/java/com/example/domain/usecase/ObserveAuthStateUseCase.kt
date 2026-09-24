package com.example.domain.usecase

import com.example.domain.model.AuthState
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use-case для наблюдения за текущим состоянием авторизации пользователя.
 *
 * Предоставляет реактивный поток [Flow] состояний [AuthState], что позволяет
 * UI-слою автоматически реагировать на изменения сессии (например, при логауте или истечении токенов).
 *
 * @param repository Репозиторий аутентификации, предоставляющий поток состояний.
 */
class ObserveAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Возвращает поток состояний авторизации.
     *
     * @return [Flow], эмитирующий объекты [AuthState].
     */
    operator fun invoke(): Flow<AuthState> {
        return repository.getAuthState()
    }
}