package com.example.domain.model

/**
 * Модель профиля пользователя в системе.
 *
 * @property zoneId Код зоны разгрузки бетона.
 * @property zoneName Наименование подразделения.
 * @property departmentId Код подразделения.
 * @property postId код поста заказа продукции (операторской панели,
 *                  управляющей одной или несколькими точками выгрузки).
 */
data class DstPoint(
    val zoneId: Int,
    val zoneName: String,
    val departmentId: String,
    val postId: Int?
)