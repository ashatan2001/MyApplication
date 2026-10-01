package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.DstPoint
import com.example.domain.usecase.GetDstPointsListUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Состояние UI для экрана со списком точек выгрузки бетона.
 *
 * Используется [StateFlow] для реактивного обновления UI:
 * - [Loading] — отображается спиннер во время загрузки
 * - [Success] — отображается список точек выгрузки бетона
 * - [Error] — отображается сообщение об ошибке с возможностью retry
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети
 */
sealed class DstPointsListUiState {
    data object Loading : DstPointsListUiState()
    data class Success(val dstPointsList: List<DstPoint>) : DstPointsListUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : DstPointsListUiState()
}

/**
 * ViewModel экрана со списком точек выгрузки бетона. Реагирует на глобальные события аутентификации
 * через [authEventBus] для корректного сброса состояния при выходе.»
 *
 * @param getDstPointsListUseCase UseCase для получения списка точек выгрузки бетона.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class DstPointsListViewModel @Inject constructor(
    private val getDstPointsListUseCase: GetDstPointsListUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<DstPointsListUiState>(DstPointsListUiState.Loading)
    val uiState: StateFlow<DstPointsListUiState> = _uiState.asStateFlow()

    init {
        Timber.d("DstPointViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = DstPointsListUiState.Loading
            }
        }
    }

    /**
     * Загружает данные точки выгрузки бетона из репозитория.
     *
     * При истёкшей сессии ничего не делает — [TokenInterceptor] уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadDstPointsList() {
        Timber.d("Начало загрузки данных точки выгрузки бетона")
        viewModelScope.launch {
            _uiState.value = DstPointsListUiState.Loading
            when (val result = getDstPointsListUseCase()) {
                is CustomResult.Success -> {
                    Timber.d("Данные точки выгрузки бетона успешно загружены")
                    _uiState.value = DstPointsListUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            // TokenInterceptor уже обработал редирект на логин
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is NetworkConnectionException -> {
                            Timber.w("Ошибка сети при загрузке данных точки выгрузки бетона")
                            "Нет соединения с интернетом"
                        }
                        else -> {
                            Timber.e(result.exception, "Неожиданная ошибка при загрузке данных точки выгрузки бетона")
                            result.exception.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = DstPointsListUiState.Error(message)
                }
            }
        }
    }
}