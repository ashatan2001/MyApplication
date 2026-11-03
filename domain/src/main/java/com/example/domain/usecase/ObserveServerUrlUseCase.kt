package com.example.domain.usecase

import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase для наблюдения за текущим URL сервера.
 * Возвращает поток значений URL из репозитория.
 *
 * @param settingsRepository Реализация [SettingsRepository].
 */
class ObserveServerUrlUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Возвращает реактивный поток с текущим URL сервера.
     *
     * @return [Flow] с текущим URL.
     */
    operator fun invoke(): Flow<String> = settingsRepository.serverUrl
}