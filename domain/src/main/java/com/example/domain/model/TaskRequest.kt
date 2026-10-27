package com.example.domain.model

/**
 * Модель продукта.
 *
 *  @property quantity Количество бетона.
 *  @property zone Номер точки выгрузки бетона.
 *  @property pallete Номер паллеты или формы, для которой предназначена заявка.
 *  @property product Код продукта.
 */

data class TaskRequest (
    val quantity: Float,
    val zone: Int?,
    val pallete: String? = null,
    val product: Long
)