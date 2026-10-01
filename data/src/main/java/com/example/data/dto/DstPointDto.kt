package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные точки выгрузки бетона от сервера (эндпоинт `api/apb/dstpoints/:zoneid`).
 *
 * Сервер возвращает поля с именами в стиле PascalCase и историческими
 * названиями ("FIO" вместо "userName", "PersonID" вместо "id").
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.DstPointMapper].
 *
 * @property zoneId Код зоны разгрузки бетона (поле ZoneNo).
 * @property zoneName Наименование подразделения.
 * @property departmentId Код подразделения.
 * @property postId код поста заказа продукции (операторской панели,
 *                  управляющей одной или несколькими точками выгрузки).
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class DstPointDto (
    @SerialName("ZoneNo") val zoneId: Int,
    @SerialName("ZoneName") val zoneName: String,
    @SerialName("DepartmentID") val departmentId: String,
    @SerialName("PostID") val postId: Int? = null
    )
