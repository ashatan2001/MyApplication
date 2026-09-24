package com.example.domain.repository

import com.example.domain.model.UserModel

/**
 * Репозиторий для получения данных текущего пользователя.
 */
interface UserRepository {

    /**
     * Возвращает полную информацию о текущем авторизованном пользователе.
     *
     * @return [UserModel] с данными профиля.
     * @throws com.example.domain.exception.UserNotFoundException если пользователь не найден.
     * @throws com.example.domain.exception.NetworkException при отсутствии соединения.
     */
    suspend fun getUserInfo(): UserModel

    /**
     * Возвращает ID текущего пользователя из локального хранилища.
     *
     * @return ID пользователя или null, если сессия не активна.
     */
    suspend fun getUserId(): Int?

    /**
     * Возвращает имя текущего пользователя из кэша без сетевого запроса.
     *
     * @return Имя пользователя или null, если данные не загружены.
     */
    suspend fun getUserName(): String?
}