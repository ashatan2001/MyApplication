package com.example.domain.model

/**
 * Данные для изменения заявки на отгрузку в очереди терминала.
 *
 * В отличие от [TaskRequest] (создание заявки), содержит полный контекст
 * уже существующей заявки: терминал, продукт и подразделение — всё,
 * что сервер требует для её изменения.
 *
 * @property terminalId Код терминала.
 * @property terminalNo Номер терминала.
 * @property productId Код продукта.
 * @property productNo Номер продукта.
 * @property quantity Новое количество отгруженной продукции.
 * @property departmentId Код подразделения.
 */
data class LoadUpdateRequest(
    val terminal: Terminal,
    val product: Product,
    val quantity: Float,
    val departmentId: String
)