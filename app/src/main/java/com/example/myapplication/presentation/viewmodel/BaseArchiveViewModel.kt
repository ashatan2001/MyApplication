package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Archive
import com.example.domain.util.CustomResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Состояние UI для экрана архивов.
 *
 * Используется [StateFlow] для реактивного обновления UI:
 * - [Loading] — отображается спиннер во время загрузки
 * - [Success] — отображаются данные архива
 * - [Error] — отображается сообщение об ошибке с возможностью retry
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети
 */
sealed interface ArchiveUiState<out T> {
    data object Loading : ArchiveUiState<Nothing>
    data class Success<T>(
        val archive: Archive<T>,
        val isLoadingMore: Boolean = false,
        val error: String? = null
    ) : ArchiveUiState<T>
    data class Error(val message: String, val isNetworkError: Boolean = false) : ArchiveUiState<Nothing>
}

/**
 * Базовый ViewModel для экрана архивов с пагинацией.
 * Инкапсулирует общую логику: первая загрузка, подгрузка следующей страницы,
 * обработка ошибок через [CustomResult].
 * Реагирует на глобальные события аутентификации
 * через [authEventBus] для корректного сброса состояния при выходе.»
 *
 * @param T тип доменной сущности архива.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
abstract class BaseArchiveViewModel<T: Any> : ViewModel() {

    private val _uiState = MutableStateFlow<ArchiveUiState<T>>(ArchiveUiState.Loading)
    val uiState: StateFlow<ArchiveUiState<T>> = _uiState.asStateFlow()

    // Сохраняем параметры запроса для пагинации
    private var currentStartDate: String = ""
    private var currentEndDate: String = ""
    private var currentRecords: Int = 50

    /**
     * Абстрактный метод, который наследник реализует вызовом своего UseCase.
     */
    protected abstract suspend fun fetchArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int
    ): CustomResult<Archive<T>>

    /**
     * Начальная загрузка архива (сбрасывает состояние).
     */
    fun loadArchive(startDate: String, endDate: String, records: Int = 50) {
        currentStartDate = startDate
        currentEndDate = endDate
        currentRecords = records

        viewModelScope.launch {
            _uiState.value = ArchiveUiState.Loading
            val result = fetchArchive(startDate, endDate, pageNum = 1, records = records)
            handleResult(result, isNewLoad = true)
        }
    }

    /**
     * Загрузка следующей страницы (добавляет данные к существующим).
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState !is ArchiveUiState.Success) return

        val archive = currentState.archive
        if (!archive.hasNextPage || currentState.isLoadingMore) return

        viewModelScope.launch {
            // Показываем индикатор загрузки внизу списка, не затирая основные данные
            _uiState.value = currentState.copy(isLoadingMore = true, error = null)

            val result = fetchArchive(
                startDate = currentStartDate,
                endDate = currentEndDate,
                pageNum = archive.nextPage!!,
                records = currentRecords
            )
            handleResult(result, isNewLoad = false)
        }
    }

    /**
     * Очистка ошибки snackbar без перезагрузки данных.
     */
    fun clearError() {
        _uiState.update { currentState ->
            if (currentState is ArchiveUiState.Success) {
                currentState.copy(error = null)
            } else {
                currentState
            }
        }
    }

    private fun handleResult(result: CustomResult<Archive<T>>, isNewLoad: Boolean) {
        when (result) {
            is CustomResult.Success -> {
                if (isNewLoad) {
                    _uiState.value = ArchiveUiState.Success(archive = result.data)
                } else {
                    _uiState.update { currentState ->
                        if (currentState is ArchiveUiState.Success) {
                            // Объединяем старый список с новым, сохраняя актуальные pageNum и pages из нового ответа
                            val updatedArchive = result.data.copy(
                                data = currentState.archive.data + result.data.data
                            )
                            currentState.copy(
                                archive = updatedArchive,
                                isLoadingMore = false
                            )
                        } else currentState
                    }
                }
            }
            is CustomResult.Error -> {
                // Примечание: адаптируйте проверку isNetworkError под вашу реализацию AppException
                val isNetworkError = result.exception.message?.contains("network", ignoreCase = true) == true

                if (isNewLoad) {
                    _uiState.value = ArchiveUiState.Error(
                        message = result.exception.message ?: "Неизвестная ошибка",
                        isNetworkError = isNetworkError
                    )
                } else {
                    _uiState.update { currentState ->
                        if (currentState is ArchiveUiState.Success) {
                            currentState.copy(
                                isLoadingMore = false,
                                error = result.exception.message ?: "Ошибка загрузки данных"
                            )
                        } else currentState
                    }
                }
            }
        }
    }
}