package com.example.domain.model

/**
 * Модель продукта.
 *
 * @property zone Номер точки выгрузки бетона.
 * @property load Код созданной заявки.
 * @property queueSize Количество заявок в очереди для указанной точки выгрузки.
 * @property terminal Номер смесителя, с которого планируется производство бетона.
 */
data class TaskResult (
    val zone: Int,
    val load: Long,
    val queueSize: Int,
    val terminal: Int
)