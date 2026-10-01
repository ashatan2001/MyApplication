package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Данные архива автоматизированной системы.
 *
 * Сервер возвращает поля с именами в стиле PascalCase и историческими названиями.
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.DstPointMapper].
 *
 * @property startDate Дата и время начала периода
 * @property endDate Дата и время окончания периода
 * @property pageNum Номер текущей просматриваемой страницы с данными.
 * @property pages Общее количество страниц, которые доступны за выбранный промежуток времени.
 * @property records Количество записей на одну страницу
 * @property data Данные из архива
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class ArchiveDto<T> (
    @SerialName("StartDate") val startDate: String,
    @SerialName("EndDate") val endDate: String,
    @SerialName("PageNo") val pageNum: Int,
    @SerialName("Pages") val pages: Int,
    @SerialName("Records") val records: Int,
    @SerialName("Data") val data: List<T>
)
