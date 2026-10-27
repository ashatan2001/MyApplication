package com.example.myapplication.presentation

import com.example.domain.model.Event
import com.example.domain.model.HandLoad

/**
 * Типы архивов, доступные для просмотра.
 *
 * @property title Отображаемое название типа архива.
 */
enum class ArchiveType(val title: String) {
    EVENTS("Архив событий"),
    HAND_LOADS("Архив ручных отгрузок")
}

/**
 * Универсальное представление строки таблицы для UI.
 * Позволяет отображать разные типы данных (события и ручные отгрузки) в одном списке.
 */
sealed interface ArchiveRow {
    /**
     * Строка, представляющая событие из архива.
     *
     * @property event Модель события.
     */
    data class EventRow(val event: Event) : ArchiveRow

    /**
     * Строка, представляющая ручную отгрузку из архива.
     *
     * @property handLoad Модель ручной отгрузки.
     */
    data class HandLoadRow(val handLoad: HandLoad) : ArchiveRow
}

/**
 * Состояние экрана архива.
 *
 * @property selectedType Выбранный тип архива.
 * @property startDate Строка с датой и временем начала периода.
 * @property endDate Строка с датой и временем окончания периода.
 * @property records Количество записей на странице.
 * @property rows Список строк для отображения в таблице.
 * @property isLoading Флаг первичной загрузки данных или смены фильтров.
 * @property isLoadingPage Флаг загрузки конкретной страницы (переход между страницами).
 * @property currentPage Номер текущей отображаемой страницы.
 * @property totalPages Общее количество страниц.
 * @property error Сообщение об ошибке, если она произошла.
 */
data class ArchiveScreenState(
    val selectedType: ArchiveType = ArchiveType.EVENTS,
    val startDate: String = "",
    val endDate: String = "",
    val records: String = "50",
    val rows: List<ArchiveRow> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val error: String? = null
)