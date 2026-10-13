package com.example.domain.repository

import com.example.domain.model.DstPoint

/**
 * Репозиторий для работы с точками выгрузки бетона.
 */
interface DstPointRepository {

    /**
     * Возвращает полную информацию о конкретной точки выгрузки бетона.
     *
     * @param zoneId код точки выгрузки бетона
     * @return [DstPoint] с данными точки выгрузки бетона.
     * @throws com.example.domain.exception.DstPointNotFoundException если точка выгрузки бетона не найдена.
     * @throws com.example.domain.exception.NetworkException при отсутствии соединения.
     */
    suspend fun getDstPoint(zoneId: Int): DstPoint

    /**
     * Возвращает список точек выгрузки бетона.
     *
     * @return [List<DstPoint>] список с точками выгрузки бетона.
     * @throws com.example.domain.exception.NetworkException при отсутствии соединения.
     */
    suspend fun getDstPointList(): List<DstPoint>
}