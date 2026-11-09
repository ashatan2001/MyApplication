package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель бетоновозной телеги, получаемая от сервера.
 *
 * @property id Код бетоновозной телеги.
 * @property name Наименование бетоновозной телеги.
 * @property zoneNumber Номер участка, на котором находится бетоновозная телега.
 * @property isFull Признак заполненной телеги.
 * @property status Статус бетонной телеги (1 - телега находится остановлена и находится в ожидании загрузки под смесителем
 *                                 2 - телега движется
 *                                 4 - телега остановлена и зафиксирована в произвольной точке
 *                                 8 - идет разгрузка телеги
 *                                 16 - телега отключена (нет связи с телегой)
 *                                 32 - телега находится на ремонте
 *                                 64 - телега находится на ручном управлении).
 * @property loadId Код заявки на отгрузку, которую в настоящий момент выполняет бетоновозная телега.
 * @property dstZone Номер участка, на который осуществляется доставка бетонной смеси.
 * @property loadDate Дата и время завершения приготовления бетонной смеси на бетоносмесительном узле.
 * @property arrivalDate Дата и время прибытия бетоновозной телеги в точку разгрузки
 * @property departDate Дата и время завершения разгрузки бетоновозной телеги в точке разгрузки
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class BucketDto(
    @SerialName("bucketid")  val id: Int,
    @SerialName("bucketname") val name: String,
    @SerialName("zoneno") val zoneNumber: Int,
    @SerialName("isfull") val isFull: Boolean,
    @SerialName("status") val status: Int,
    @SerialName("loadid") val loadId: Long,
    @SerialName("dst_zone") val dstZone: Int,
    @SerialName("loaddate") val loadDate: String? = null,
    @SerialName("arrivaldate") val arrivalDate: String? = null,
    @SerialName("departdate") val departDate: String? = null
)

