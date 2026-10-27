package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.model.Archive
import com.example.domain.model.Event
import com.example.domain.model.HandLoad
import com.example.domain.usecase.GetEventArchiveUseCase
import com.example.domain.usecase.GetHandLoadArchiveUseCase
import com.example.domain.util.CustomResult
import com.example.myapplication.presentation.ArchiveRow
import com.example.myapplication.presentation.ArchiveScreenState
import com.example.myapplication.presentation.ArchiveType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления состоянием экрана архива.
 * Отвечает за загрузку данных, пагинацию и обработку пользовательских действий.
 *
 * @property getEventArchiveUseCase UseCase для получения архива событий.
 * @property getHandLoadArchiveUseCase UseCase для получения архива ручных отгрузок.
 * @property authEventBus Шина событий аутентификации для сброса состояния при выходе.
 */
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val getEventArchiveUseCase: GetEventArchiveUseCase,
    private val getHandLoadArchiveUseCase: GetHandLoadArchiveUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _state = MutableStateFlow(ArchiveScreenState())

    /**
     * Наблюдаемое состояние экрана архива.
     */
    val state: StateFlow<ArchiveScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                _state.value = ArchiveScreenState()
            }
        }
    }

    /**
     * Обновляет выбранный тип архива и сбрасывает список строк.
     *
     * @param type Новый тип архива.
     */
    fun updateType(type: ArchiveType) {
        _state.update { it.copy(selectedType = type, rows = emptyList(), error = null) }
    }

    /**
     * Обновляет дату и время начала периода.
     *
     * @param date Строка с датой.
     */
    fun updateStartDate(date: String) = _state.update { it.copy(startDate = date) }

    /**
     * Обновляет дату и время окончания периода.
     *
     * @param date Строка с датой.
     */
    fun updateEndDate(date: String) = _state.update { it.copy(endDate = date) }

    /**
     * Обновляет количество записей на странице.
     *
     * @param records Количество записей.
     */
    fun updateRecords(records: String) = _state.update { it.copy(records = records) }

    /**
     * Запускает первичную загрузку архива с первой страницы.
     * Проверяет заполненность дат перед выполнением запроса.
     */
    fun loadArchive() {
        val currentState = _state.value
        if (currentState.startDate.isBlank() || currentState.endDate.isBlank()) {
            _state.update { it.copy(error = "Укажите дату и время начала и окончания периода") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, rows = emptyList()) }
            val result = executeUseCase(currentState, pageNum = 1)
            handleResult(result)
        }
    }

    /**
     * Выполняет переход на указанную страницу архива.
     * Заменяет текущие данные новыми, а не дополняет их.
     *
     * @param page Номер страницы для загрузки.
     */
    fun goToPage(page: Int) {
        val currentState = _state.value
        if (page < 1 || page > currentState.totalPages || currentState.isLoadingPage || currentState.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingPage = true, error = null) }
            val result = executeUseCase(currentState, pageNum = page)
            handleResult(result)
        }
    }

    /**
     * Переходит на следующую страницу.
     */
    fun nextPage() = goToPage(_state.value.currentPage + 1)

    /**
     * Переходит на предыдущую страницу.
     */
    fun previousPage() = goToPage(_state.value.currentPage - 1)

    /**
     * Очищает сообщение об ошибке.
     */
    fun clearError() = _state.update { it.copy(error = null) }

    /**
     * Выполняет соответствующий UseCase в зависимости от выбранного типа архива.
     *
     * @param state Текущее состояние экрана.
     * @param pageNum Номер запрашиваемой страницы.
     * @return Результат выполнения запроса.
     */
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
                getHandLoadArchiveUseCase(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    pageNum = pageNum,
                    records = recordsInt
                )
            }
        }
    }

    /**
     * Обрабатывает результат запроса к API.
     * При успешном ответе заменяет текущие строки новыми и обновляет информацию о пагинации.
     *
     * @param result Результат выполнения запроса.
     */
    private fun handleResult(result: CustomResult<Archive<*>>) {
        when (result) {
            is CustomResult.Success -> {
                val archive = result.data
                val newRows = mapToRows(archive.data)
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isLoadingPage = false,
                        rows = newRows, // Полная замена данных
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
                        isLoadingPage = false,
                        error = result.exception.message ?: "Ошибка загрузки данных"
                    )
                }
            }
        }
    }

    /**
     * Преобразует список доменных моделей в список строк для отображения в UI.
     *
     * @param data Список объектов из ответа API.
     * @return Список объектов [ArchiveRow].
     */
    private fun mapToRows(data: List<*>): List<ArchiveRow> {
        return data.mapNotNull { item ->
            when (item) {
                is Event -> ArchiveRow.EventRow(item)
                is HandLoad -> ArchiveRow.HandLoadRow(item)
                else -> null
            }
        }
    }
}