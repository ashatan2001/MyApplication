package com.example.domain.usecase

import com.example.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * UseCase для сброса URL сервера к значению по умолчанию.
 *
 * @param settingsRepository Реализация [SettingsRepository].
 */
class ResetServerUrlUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Сбрасывает настройки URL к значению по умолчанию.
     */
    suspend operator fun invoke() {
        settingsRepository.resetToDefault()
    }
}