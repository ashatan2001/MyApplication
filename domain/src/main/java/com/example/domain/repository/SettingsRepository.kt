package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для управления настройками приложения.
 * Отвечает за хранение и получение конфигурации, такой как URL сервера.
 */
interface SettingsRepository {

    /**
     * Реактивный поток с текущим URL сервера.
     * Если пользователь не задал свой URL, возвращается значение по умолчанию из BuildConfig.
     */
    val serverUrl: Flow<String>

    /**
     * Сохраняет новый URL сервера в локальное хранилище.
     *
     * @param url Новый адрес сервера.
     */
    suspend fun saveServerUrl(url: String)

    /**
     * Сбрасывает URL к значению по умолчанию, удаляя сохранённое значение.
     */
    suspend fun resetToDefault()

    /**
     * Проверяет доступность сервера по указанному URL.
     * Выполняет сетевой запрос (HEAD) к корню адреса.
     *
     * @param url Адрес сервера для проверки.
     * @throws com.example.domain.exception.ServerApiException если сервер недоступен или вернул ошибку.
     * @throws com.example.domain.exception.NetworkConnectionException при отсутствии сети.
     */
    suspend fun testConnection(url: String)
}