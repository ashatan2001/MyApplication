package com.example.domain.repository

import com.example.domain.model.DstPoint

/**
 * Репозиторий для получения данных текущего пользователя.
 */
interface DstPointsRepository {

    /**
     * Возвращает полную информацию о конкретной точки выгрузки бетона.
     *
     * @return [DstPoint] с данными точки выгрузки бетона.
     * @throws com.example.domain.exception.UserNotFoundException если точка выгрузки бетона не найдена.
     * @throws com.example.domain.exception.NetworkException при отсутствии соединения.
     */
    suspend fun getDstPoint(zoneId: Int): DstPoint

    /**
     * Возвращает список точек выгрузки бетона.
     *
     * @return [List<DstPoint>] список с точками выгрузки бетона.
     * @throws com.example.domain.exception.NetworkException при отсутствии соединения.
     */
    suspend fun getDstPointsList(): List<DstPoint>
}