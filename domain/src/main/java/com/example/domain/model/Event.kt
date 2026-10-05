package com.example.domain.model

import java.time.LocalDateTime

/**
 * Модель профиля пользователя в системе.
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
data class Event(
    val id: Int,
    val date: LocalDateTime,
    val person: Person,
    val equipment: Equipment,
    val deviceName: String?,
    val workPlaceName: String?,
    val className: String?,
    val text: String?
)