package com.example.myapplication.presentation

import com.example.domain.model.Event

// Типы архивов из вашего ArchiveApi
enum class ArchiveType(val title: String) {
    EVENTS("Архив событий"),
    HAND_LOADS("Архив ручных отгрузок")
}

// Универсальное представление строки таблицы для UI
sealed interface ArchiveRow {
    data class EventRow(val event: Event) : ArchiveRow
    // data class HandLoadRow(val handLoad: HandLoad) : ArchiveRow
}

// Состояние экрана
data class ArchiveScreenState(
    val selectedType: ArchiveType = ArchiveType.EVENTS,
    val startDate: String = "",
    val endDate: String = "",
    val records: String = "50",
    val rows: List<ArchiveRow> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val error: String? = null
)