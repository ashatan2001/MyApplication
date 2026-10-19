package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель заявки на отгрузку, получаемая от сервера.
 *
 * @property id Код заявки на отгрузку.
 * @property date Дата и время отгрузки.
 * @property documentNum Номер накладной.
 * @property queueNum Номер в очереди (только для заявок в очереди).
 * @property status Статус заявки (0 - в очереди, 1 - выполнена, -1 - брак, -2 - аннулирована, 3 - взвешена).
 * @property statusName Наименование статуса заявки.
 * @property arrivalDate Дата и время прибытия на площадку.
 * @property departDate Дата и время убытия.
 * @property transport Информация о транспортном средстве.
 * @property terminal Информация об отгрузочном терминале.
 * @property product Информация о продукте.
 * @property quality Количество отгруженной продукции по заданию.
 * @property creator Информация о сотруднике, создавшем заявку.
 * @property operator Информация о сотруднике, выполнившем отгрузку.
 * @property accuracyMode Режим компенсации погрешностей дозирования.
 * @property department Информация о подразделении.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class LoadDto(
    @SerialName("LoadID") val id: Long,
    @SerialName("LoadDate") val date: String,
    @SerialName("LoadDocNo") val documentNum: String? = null,
    @SerialName("QueueNo") val queueNum: Int? = null,
    @SerialName("Status") val status: Int,
    @SerialName("StatusName") val statusName: String,
    @SerialName("ArrivalDate") val arrivalDate: String? = null,
    @SerialName("DepartDate") val departDate: String? = null,
    @SerialName("Auto") val transport: TransportDto? = null,
    @SerialName("Terminal") val terminal: TerminalDto? = null,
    @SerialName("Product") val product: ProductDto? = null,
    @SerialName("Qty") val quality: Float,
    @SerialName("Creator") val creator: PersonDto? = null,
    @SerialName("Operator") val operator: PersonDto? = null,
    @SerialName("AccuracyMode") val accuracyMode: String? = null,
    @SerialName("Department") val department: DepartmentDto? = null
)

