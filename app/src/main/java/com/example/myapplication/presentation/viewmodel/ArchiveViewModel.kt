package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.usecase.GetEventArchiveUseCase
import com.example.domain.usecase.GetHandLoadArchiveUseCase
import com.example.domain.util.CustomResult
import com.example.myapplication.presentation.util.ArchiveRow
import com.example.myapplication.presentation.util.ArchiveScreenState
import com.example.myapplication.presentation.util.ArchiveType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Унифицированный результат загрузки страницы архива.
 *
 * Сводит результаты [GetEventArchiveUseCase] и [GetHandLoadArchiveUseCase]
 * к общему виду, чтобы [handleResult] работал одинаково для обоих типов архива.
 *
 * @property rows Преобразованные строки таблицы.
 * @property pageNum Номер полученной страницы.
 * @property pages Общее количество страниц.
 */
private data class ArchiveRowBatch(
    val rows: List<ArchiveRow>,
    val pageNum: Int,
    val pages: Int
)

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
     * @param date Строка с датой в формате "dd.MM.yyyy HH:mm:ss".
     */
    fun updateStartDate(date: String) = _state.update { it.copy(startDate = date) }

    /**
     * Обновляет дату и время окончания периода.
     *
     * @param date Строка с датой в формате "dd.MM.yyyy HH:mm:ss".
     */
    fun updateEndDate(date: String) = _state.update { it.copy(endDate = date) }

    /**
     * Обновляет количество записей на странице.
     *
     * @param records Количество записей ("25", "50", "100", "200").
     */
    fun updateRecords(records: String) = _state.update { it.copy(records = records) }

    /**
     * Запускает первичную загрузку архива с первой страницы.
     *
     * Валидирует заполненность дат и корректность диапазона
     * (начало ≤ конец) перед выполнением запроса.
     */
    fun loadArchive() {
        val currentState = _state.value
        val validationError = validatePeriod(currentState.startDate, currentState.endDate)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
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
     *
     * Заменяет текущие данные новыми, а не дополняет их.
     *
     * @param page Номер страницы для загрузки.
     */
    fun goToPage(page: Int) {
        val currentState = _state.value
        if (page < 1 ||
            page > currentState.totalPages ||
            currentState.isLoadingPage ||
            currentState.isLoading
        ) return
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
     * Возвращает унифицированный [ArchiveRowBatch], чтобы обработчик результата
     * не зависел от конкретного типа данных.
     *
     * @param state Текущее состояние экрана.
     * @param pageNum Номер запрашиваемой страницы.
     * @return Результат выполнения запроса.
     */
    private suspend fun executeUseCase(
        state: ArchiveScreenState,
        pageNum: Int
    ): CustomResult<ArchiveRowBatch> {
        val recordsInt = state.records.toIntOrNull() ?: 50
        return when (state.selectedType) {
            ArchiveType.EVENTS -> {
                when (val res = getEventArchiveUseCase(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    pageNum = pageNum,
                    records = recordsInt
                )) {
                    is CustomResult.Success -> CustomResult.Success(
                        ArchiveRowBatch(
                            rows = res.data.data.map { ArchiveRow.EventRow(it) },
                            pageNum = res.data.pageNum,
                            pages = res.data.pages
                        )
                    )

                    is CustomResult.Error -> res
                }
            }

            ArchiveType.HAND_LOADS -> {
                when (val res = getHandLoadArchiveUseCase(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    pageNum = pageNum,
                    records = recordsInt
                )) {
                    is CustomResult.Success -> CustomResult.Success(
                        ArchiveRowBatch(
                            rows = res.data.data.map { ArchiveRow.HandLoadRow(it) },
                            pageNum = res.data.pageNum,
                            pages = res.data.pages
                        )
                    )

                    is CustomResult.Error -> res
                }
            }
        }
    }

    /**
     * Обрабатывает результат запроса к API.
     *
     * При успешном ответе заменяет текущие строки новыми и обновляет
     * информацию о пагинации. При ошибке сохраняет сообщение для UI.
     *
     * @param result Результат выполнения запроса.
     */
    private fun handleResult(result: CustomResult<ArchiveRowBatch>) {
        when (result) {
            is CustomResult.Success -> {
                val batch = result.data
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isLoadingPage = false,
                        rows = batch.rows,
                        currentPage = batch.pageNum,
                        totalPages = batch.pages,
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
     * Валидирует введённый период дат.
     *
     * Проверяет заполненность обеих дат и корректность диапазона:
     * дата начала не должна быть позже даты окончания.
     *
     * @param startDate Строка с датой начала.
     * @param endDate Строка с датой окончания.
     * @return Текст ошибки для UI или `null`, если период корректен.
     */
    private fun validatePeriod(startDate: String, endDate: String): String? {
        if (startDate.isBlank() || endDate.isBlank()) {
            return "Укажите дату и время начала и окончания периода"
        }
        val start = parseToInstant(startDate)
        val end = parseToInstant(endDate)
        if (start == null || end == null) {
            return "Неверный формат даты"
        }
        if (start.isAfter(end)) {
            return "Дата начала не может быть позже даты окончания"
        }
        return null
    }

    /**
     * Парсит строку даты в UTC-[java.time.Instant] для сравнения.
     *
     * @param dateStr Строка в формате "dd.MM.yyyy HH:mm:ss".
     * @return Instant в UTC или null при ошибке парсинга.
     */
    private fun parseToInstant(dateStr: String): java.time.Instant? = runCatching {
        LocalDateTime.parse(dateStr, com.example.myapplication.presentation.util.DateTimeFormatters.dateTimeFormatter)
            .toInstant(ZoneOffset.UTC)
    }.getOrNull()
}