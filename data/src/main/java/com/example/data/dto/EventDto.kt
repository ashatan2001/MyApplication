package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные события из архива системы.
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.EventMapper].
 *
 * @property id идентификатор события в базе данных.
 * @property date Дата и время.
 * @property person Информация о сотруднике.
 * @property equipment Информация об оборудовании.
 * @property deviceName Наименование управляющей службы технологического процесса.
 * @property workPlaceName Наименование места, на котором возникло событие.
 * @property className Наименование класса событий
 * @property text Текст события
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class EventDto (
    @SerialName("EventID") val id: Int,
    @SerialName("EventDate") val date: String,
    @SerialName("Person") val person: PersonDto? = null,
    @SerialName("Eq") val equipment: EquipmentDto? = null,
    @SerialName("DeviceName") val deviceName: String? = null,
    @SerialName("WPName") val workPlaceName: String? = null,
    @SerialName("EventClassName") val className: String? = null,
    @SerialName("EventText") val text: String? = null
)
