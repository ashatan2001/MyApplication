package com.example.data.remote

import com.example.data.dto.LoadDto
import com.example.data.dto.TaskRequestDto
import com.example.data.dto.TaskResponseDto
import com.example.data.util.safeApiCall
import retrofit2.http.Path
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
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getLoads(): List<LoadDto> = safeApiCall {
        api.getLoads() }

    /**
     * Загружает заявки на отгрузку, находящиеся в очереди заданного отгрузочного терминала.
     *
     * @param terminalId Код отгрузочного терминала.
     * @return [List<LoadDto>] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getLoadsByTerminalId(terminalId: String): List<LoadDto> = safeApiCall {
        api.getLoadsByTerminalId(terminalId) }

    /**
     * Загружает заявки на отгрузку, находящиеся в очереди заданного отгрузочного терминала.
     *
     * @param terminalNum Номер отгрузочного терминала.
     * @return [List<LoadDto>] при успехе.
     * @throws com.example.data.exception.DataLayerException и наследники при ошибке.
     */
    suspend fun getLoadsByTerminalNum(terminalNum: Int): List<LoadDto> = safeApiCall {
        api.getLoadsByTerminalNum(terminalNum) }
}