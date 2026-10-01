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
 * @property id Идентификатор агрегата.
 * @property name Наименование агрегата.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class EquipmentDto (
    @SerialName("EqID") val id: Int,
    @SerialName("EqName") val name: String,
)
