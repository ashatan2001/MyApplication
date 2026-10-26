package com.example.domain.repository

import com.example.domain.model.Load
import com.example.domain.model.TaskRequest

/**
 * Репозиторий для управления очередью на отгрузочных терминалах.
 */
interface QueueRepository {
    /**
     * Возвращает список заявок, находящихся в очереди на отгрузку.
     *
     * @return [List<Load>] список заявок.
     */
    suspend fun getLoads(): List<Load>

    /**
     * Возвращает список заявок, находящихся в очереди заданного отгрузочного терминала
     *
     * @param terminalId Код отгрузочного терминала.
     * @return [List<Load>] список заявок.
     */
    suspend fun getLoadsByTerminalId(terminalId: String): List<Load>

    /**
     * Возвращает список заявок, находящихся в очереди заданного отгрузочного терминала
     *
     * @param terminalNum Номер отгрузочного терминала.
     * @return [List<Load>] список заявок.
     */
    suspend fun getLoadsByTerminalNum(terminalNum: Int): List<Load>

    /**
     * Изменение заявки, находящейся в очереди отгрузочного терминала
     *
     * @param loadId код заявки на отгрузку.
     * @param task новые данные заявки.
     */
    suspend fun updateLoadById(loadId: String, task: TaskRequest): Long

    /**
     * Перемещение заявки в очереди заданного отгрузочного терминала
     *
     * @param loadId код заявки на отгрузку.
     * @param number номер заявки в очереди.
     */
    suspend fun updateQueueNumLoadById(loadId: String, number: Int): Long

    /**
     * Удаление заявки из очереди отгрузочного терминала
     *
     * @param loadId код заявки на отгрузку.
     */
    suspend fun deleteLoadById(loadId: String)
}