package com.example.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.model.Archive
import com.example.domain.model.Event
import com.example.domain.usecase.GetEventArchiveUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val getEventArchiveUseCase: GetEventArchiveUseCase,
    private val authEventBus: AuthEventBus // Инжектим шину событий
) : ViewModel() {

    private val _state = MutableStateFlow(ArchiveScreenState())
    val state: StateFlow<ArchiveScreenState> = _state.asStateFlow()

    init {
        // Реагируем на принудительный выход из системы (например, истечение сессии 419/401)
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                // Сбрасываем состояние экрана, чтобы не оставлять чувствительные данные
                _state.value = ArchiveScreenState()
            }
        }
    }

    // Обновление полей формы
    fun updateType(type: ArchiveType) {
        _state.update { it.copy(selectedType = type, rows = emptyList(), error = null) }
    }

    fun updateStartDate(date: String) = _state.update { it.copy(startDate = date) }
    fun updateEndDate(date: String) = _state.update { it.copy(endDate = date) }
    fun updateRecords(records: String) = _state.update { it.copy(records = records) }

    // Основная загрузка
    fun loadArchive() {
        val currentState = _state.value
        if (currentState.startDate.isBlank() || currentState.endDate.isBlank()) {
            _state.update { it.copy(error = "Укажите даты начала и окончания периода") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, rows = emptyList()) }

            val result = executeUseCase(currentState, pageNum = 1)
            handleResult(result, isNewLoad = true)
        }
    }

    // Подгрузка следующей страницы
    fun loadNextPage() {
        val currentState = _state.value
        val hasNextPage = currentState.currentPage < currentState.totalPages

        if (currentState.isLoadingMore || !hasNextPage) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true, error = null) }

            val result = executeUseCase(currentState, pageNum = currentState.currentPage + 1)
            handleResult(result, isNewLoad = false)
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    // Маршрутизация к нужному UseCase
    private suspend fun executeUseCase(state: ArchiveScreenState, pageNum: Int): CustomResult<Archive<*>> {
        val recordsInt = state.records.toIntOrNull() ?: 50

        return when (state.selectedType) {
            ArchiveType.EVENTS -> {
                getEventArchiveUseCase(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    pageNum = pageNum,
                    records = recordsInt
                )
            }
            ArchiveType.HAND_LOADS -> {
                // return getHandLoadArchiveUseCase(state.startDate, state.endDate, pageNum, recordsInt)
                TODO("Реализовать GetHandLoadArchiveUseCase по аналогии с Event")
            }
        }
    }

    private fun handleResult(result: CustomResult<Archive<*>>, isNewLoad: Boolean) {
        when (result) {
            is CustomResult.Success -> {
                val archive = result.data
                val newRows = mapToRows(archive.data)

                _state.update { currentState ->
                    val updatedRows = if (isNewLoad) newRows else currentState.rows + newRows
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        rows = updatedRows,
                        currentPage = archive.pageNum,
                        totalPages = archive.pages,
                        error = null
                    )
                }
            }
            is CustomResult.Error -> {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.exception.message ?: "Ошибка загрузки данных"
                    )
                }
            }
        }
    }

    // Маппинг доменных моделей в UI-модели для таблицы
    private fun mapToRows(data: List<*>): List<ArchiveRow> {
        return data.mapNotNull { item ->
            when (item) {
                is Event -> ArchiveRow.EventRow(item)
                // is HandLoad -> ArchiveRow.HandLoadRow(item)
                else -> null // Игнорируем неизвестные типы вместо падения с исключением
            }
        }
    }
}
    private fun handleResult(result: CustomResult<Archive<*>>, isNewLoad: Boolean) {
        when (result) {
            is CustomResult.Success -> {
                val archive = result.data
                val newRows = mapToRows(archive.data)

                _state.update { currentState ->
                    val updatedRows = if (isNewLoad) newRows else currentState.rows + newRows
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        rows = updatedRows,
                        currentPage = archive.pageNum,
                        totalPages = archive.pages,
                        error = null
                    )
                }
            }
            is CustomResult.Error -> {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.exception.message ?: "Ошибка загрузки данных"
                    )
                }
            }
        }
    }

    // Маппинг доменных моделей в UI-модели для таблицы
    private fun mapToRows(data: List<*>): List<ArchiveRow> {
        return data.map { item ->
            when (item) {
                is Event -> ArchiveRow.EventRow(item)
                // is HandLoad -> ArchiveRow.HandLoadRow(item)
                else -> throw IllegalArgumentException("Unknown archive item type")
            }
        }
    }
}