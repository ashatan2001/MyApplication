package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные терминала от сервера.
 *
 * Маппинг в доменную модель выполняется в [com.example.data.mapper.TerminalMapper].
 *
 * @property id Код терминала
 * @property number Номер терминала
 * @property name Наименование терминала
 * @property measure Единица измерения терминала
 * @property type Тип терминала
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class TerminalDto (
    @SerialName("TerminalID") val id: String,
    @SerialName("TerminalNo") val number: Int,
    @SerialName("TerminalName") val name: String,
    @SerialName("MeasureID") val measure: String? = null,
    @SerialName("TerminalType") val type: Int,
)
