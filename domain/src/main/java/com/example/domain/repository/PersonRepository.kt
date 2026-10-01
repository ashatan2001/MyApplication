package com.example.domain.repository

import com.example.domain.model.Person

/**
 * Репозиторий для получения данных текущего пользователя.
 */
interface PersonRepository {

    /**
     * Возвращает полную информацию о текущем авторизованном пользователе.
     *
     * @return [Person] с данными профиля.
     * @throws com.example.domain.exception.PersonNotFoundException если пользователь не найден.
     * @throws com.example.domain.exception.NetworkException при отсутствии соединения.
     */
    suspend fun getPersonInfo(): Person

    /**
     * Возвращает ID текущего пользователя из локального хранилища.
     *
     * @return ID пользователя или null, если сессия не активна.
     */
    suspend fun getPersonId(): Int?

    /**
     * Возвращает имя текущего пользователя из кэша без сетевого запроса.
     *
     * @return Имя пользователя или null, если данные не загружены.
     */
    suspend fun getPersonName(): String?
}