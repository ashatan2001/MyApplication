package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.repository.SettingsRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

/**
 * UseCase для проверки доступности сервера по указанному URL.
 * Инкапсулирует бизнес-логику тестирования подключения и маппинг исключений.
 *
 * @param settingsRepository Реализация [SettingsRepository] для выполнения сетевой проверки.
 */
class TestConnectionUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Выполняет проверку подключения к указанному URL.
     *
     * @param url Адрес сервера для проверки.
     * @return [CustomResult] содержащий [Unit] при успехе или обернутую ошибку.
     */
    suspend operator fun invoke(url: String): CustomResult<Unit> {
        return try {
            settingsRepository.testConnection(url)
            CustomResult.Success(Unit)
        } catch (e: Exception) {
            // Преобразуем все исключения в AppException для единой обработки в UI
            CustomResult.Error(AppException(message = e.message ?: "Ошибка подключения", cause = e))
        }
    }
}