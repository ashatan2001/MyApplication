package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ сервера при успешном создании заявки на отгрузку.
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.TaskMapper].
 *
 * @property zone Номер точки выгрузки бетона.
 * @property load Код созданной заявки.
 * @property queueSize Количество заявок в очереди для указанной точки выгрузки.
 * @property terminal Номер смесителя, с которого планируется производство бетона.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class TaskResponseDto (
    @SerialName("ZoneNo") val zone: Int,
    @SerialName("LoadID") val load: Long,
    @SerialName("QueueSize") val queueSize: Int,
    @SerialName("TerminalNo") val terminal: Int
){
}
