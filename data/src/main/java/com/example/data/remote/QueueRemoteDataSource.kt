package com.example.data.remote

import LoadUpdateRequestDto
import com.example.data.dto.LoadDto
import com.example.data.dto.LoadIdResponseDto
import com.example.data.util.safeApiCall
import javax.inject.Inject

/**
 * Сервис-обертка над Retrofit API для управления очередью на отгрузочных терминалах.
 *
 * Изолирует детали сетевых запросов (сериализация, обработка ошибок)
 * от репозитория. Все методы обёрнуты в [safeApiCall], который
 * автоматически маппит сетевые ошибки в исключения дата-слоя.
 */
class QueueRemoteDataSource @Inject constructor(
    private val api: QueueApi
) {
    /**
     * Загружает заявки на отгрузку, находящиеся в очереди.
     *
     * @return [List<LoadDto>] при успехе.
     */
    suspend fun getLoads(): List<LoadDto> = safeApiCall {
        api.getLoads() }

    /**
     * Загружает заявки на отгрузку, находящиеся в очереди заданного отгрузочного терминала.
     *
     * @param terminalId Код отгрузочного терминала.
     * @return [List<LoadDto>] при успехе.
     */
    suspend fun getLoadsByTerminalId(terminalId: String): List<LoadDto> = safeApiCall {
        api.getLoadsByTerminalId(terminalId) }

    /**
     * Загружает заявки на отгрузку, находящиеся в очереди заданного отгрузочного терминала.
     *
     * @param terminalNum Номер отгрузочного терминала.
     * @return [List<LoadDto>] при успехе.
     */
    suspend fun getLoadsByTerminalNum(terminalNum: Int): List<LoadDto> = safeApiCall {
        api.getLoadsByTerminalNum(terminalNum) }

    /**
     * Изменяет заявку на отгрузку, находящуюся в очереди отгрузочного терминала.
     *
     * @param loadId код заявки на отгрузку.
     * @param request новые данные заявки.
     * @return [LoadIdResponseDto] при успехе.
     */
    suspend fun updateLoadById(loadId: Long, request: LoadUpdateRequestDto): LoadIdResponseDto = safeApiCall {
        api.putLoadById(loadId, request) }

    /**
     * Изменяет заявку на отгрузку, находящуюся в очереди отгрузочного терминала.
     *
     * @param loadId код заявки на отгрузку.
     * @param number номер заявки в очереди.
     * @return [LoadIdResponseDto] при успехе.
     */
    suspend fun updateQueueNumLoadById(loadId: Long, number: String) : LoadIdResponseDto = safeApiCall {
        api.putQueueNumLoadById(loadId, number) }

    /**
     * Удаляет заявку на отгрузку, находящуюся в очереди отгрузочного терминала.
     *
     * @param loadId код заявки на отгрузку.
     */
    suspend fun deleteLoadById(loadId: Long) {
        safeApiCall { api.deleteLoadById(loadId) }
    }
}