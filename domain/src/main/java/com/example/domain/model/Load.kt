package com.example.domain.model

import java.time.LocalDateTime


/**
 * Модель заявки на отгрузку.
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

data class Load (
    val id: Long,
    val date: LocalDateTime,
    val documentNum: String?,
    val queueNum: Int?,
    val status: LoadStatus,
    val statusName: String,
    val arrivalDate: LocalDateTime?,
    val departDate: LocalDateTime?,
    val transport: Transport?,
    val terminal: Terminal?,
    val product: Product?,
    val quality: Float,
    val creator: Person?,
    val operator: Person?,
    val accuracyMode: String?,
    val department: Department?
)