package com.example.domain.repository

import com.example.domain.model.Archive
import com.example.domain.model.Event
import com.example.domain.model.HandLoad

/**
 * Репозиторий для получения данных текущего пользователя.
 */
interface ArchiveRepository {

    /**
     * Возвращает архив событий.
     *
     * @param startDate Дата и время начала периода.
     * @param endDate Дата и время окончания периода.
     * @param pageNum Номер текущей страницы.
     * @param pages Общее количество страниц за выбранный промежуток.
     * @param records Количество записей на одной странице.
     * @return [Archive<Event>] информация о событиях системы.
     */
    suspend fun getEventArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int,
    ): Archive<Event>

    /**
     * Возвращает архив событий.
     *
     * @param startDate Дата и время начала периода.
     * @param endDate Дата и время окончания периода.
     * @param pageNum Номер текущей страницы.
     * @param records Количество записей на одной странице.
     * @return [Archive<HandLoad>] информация об ручных отгрузках.
     */
    suspend fun getHandLoadArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int,
    ): Archive<HandLoad>
}