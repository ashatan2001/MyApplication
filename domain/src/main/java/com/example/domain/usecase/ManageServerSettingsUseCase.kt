package com.example.domain.usecase

import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase для управления настройками сервера.
 * Инкапсулирует бизнес-логику сохранения и сброса URL,
 * скрывая детали реализации репозитория от ViewModel.
 *
 * @param settingsRepository Реализация контракта SettingsRepository.
 */
class ManageServerSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Поток текущего URL сервера для наблюдения из ViewModel.
     */
    val serverUrl: Flow<String> = settingsRepository.serverUrl

    /**
     * Сохраняет новый URL сервера.
     *
     * @param newUrl Новый адрес сервера для сохранения.
     * @return Result<Unit> Успех или ошибка выполнения операции.
     */
    suspend operator fun invoke(newUrl: String): Result<Unit> {
        return try {
            settingsRepository.saveServerUrl(newUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Сбрасывает настройки URL к значению по умолчанию.
     *
     * @return Result<Unit> Успех или ошибка выполнения операции.
     */
    suspend fun resetToDefault(): Result<Unit> {
        return try {
            settingsRepository.resetToDefault()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}