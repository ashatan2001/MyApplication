package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Контракт репозитория настроек приложения.
 * Реализация находится в модуле data.
 */
interface SettingsRepository {

    /**
     * Поток с текущим URL сервера.
     * Если пользователь не задал свой URL, возвращается значение из BuildConfig.
     *
     * @return [Flow]<[String]> с актуальным URL сервера.
     */
    val serverUrl: Flow<String>

    /**
     * Сохраняет новый URL сервера.
     *
     * @param url Новый адрес сервера.
     */
    suspend fun saveServerUrl(url: String)

    /**
     * Сбрасывает URL к значению по умолчанию из BuildConfig.
     */
    suspend fun resetToDefault()
}