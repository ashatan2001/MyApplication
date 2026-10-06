package com.example.domain.repository

import com.example.domain.model.Archive
import com.example.domain.model.Event

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
     * @throws com.example.domain.exception.EventNotFoundException если за заданный
     *         промежуток времени события отсутствуют в системе.
     * @throws com.example.domain.exception.NetworkException при отсутствии соединения.
     */
    suspend fun getEventArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int,
    ): Archive<Event>
}