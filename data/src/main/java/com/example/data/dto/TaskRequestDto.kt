package com.example.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO модель заявки, отправляемая серверу
 *
 * @property quantity Количество бетона.
 * @property zone Номер точки выгрузки бетона.
 * @property pallete Номер паллеты или формы, для которой предназначена заявка.
 * @property product Код продукта.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class TaskRequestDto (
    @SerialName("qty") val quantity:  Float,
    @SerialName("zoneno") val zone: Int,
    @SerialName("palleteno") val pallete: String? = null,
    @SerialName("productid") val product: Long,
){
}
