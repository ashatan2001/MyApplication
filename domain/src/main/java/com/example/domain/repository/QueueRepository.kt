package com.example.domain.repository

import com.example.domain.model.Load

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
}