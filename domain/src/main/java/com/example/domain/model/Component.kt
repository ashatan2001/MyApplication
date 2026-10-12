package com.example.domain.model

/**
 * Модель компонента.
 *
 * @property productId Код компонента
 * @property productName Наименование компонента
 * @property productNum Номер компонента
 * @property density Плотность компонента
 * @property notes Примечание к компоненту
 * @property isComponent Признак того, что данный продукт является компонентом
 */

data class Component (
    val productId: Int,
    val productName: String,
    val productNum: String?,
    val density: Float?,
    val notes: String?,
    val isComponent: Boolean?
)