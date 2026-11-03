package com.example.domain.usecase

import com.example.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * UseCase для сохранения нового URL сервера.
 *
 * @param settingsRepository Реализация [SettingsRepository].
 */
class SaveServerUrlUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Сохраняет новый URL сервера в локальное хранилище.
     *
     * @param url Новый адрес сервера для сохранения.
     */
    suspend operator fun invoke(url: String) {
        settingsRepository.saveServerUrl(url)
    }
}