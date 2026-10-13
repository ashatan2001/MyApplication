package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.DstPoint
import com.example.domain.usecase.GetDstPointListUseCase
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
sealed class DstPointListUiState {
    data object Loading : DstPointListUiState()
    data class Success(val dstPointList: List<DstPoint>) : DstPointListUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : DstPointListUiState()
}

/**
 * ViewModel экрана со списком точек выгрузки бетона. Реагирует на глобальные события аутентификации
 * через [authEventBus] для корректного сброса состояния при выходе.»
 *
 * @param getDstPointListUseCase UseCase для получения списка точек выгрузки бетона.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class DstPointListViewModel @Inject constructor(
    private val getDstPointListUseCase: GetDstPointListUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<DstPointListUiState>(DstPointListUiState.Loading)
    val uiState: StateFlow<DstPointListUiState> = _uiState.asStateFlow()

    init {
        Timber.d("DstPointViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = DstPointListUiState.Loading
            }
        }
    }

    /**
     * Загружает список точек выгрузки бетона из репозитория.
     *
     * При истёкшей сессии ничего не делает — [TokenInterceptor] уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadDstPointList() {
        Timber.d("Начало загрузки списка точек выгрузки бетона")
        viewModelScope.launch {
            _uiState.value = DstPointListUiState.Loading
            when (val result = getDstPointListUseCase()) {
                is CustomResult.Success -> {
                    Timber.d("Список точек выгрузки бетона успешно загружен")
                    _uiState.value = DstPointListUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            // TokenInterceptor уже обработал редирект на логин
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is NetworkConnectionException -> {
                            Timber.w("Ошибка сети при загрузке списка точек выгрузки бетона")
                            "Нет соединения с интернетом"
                        }
                        else -> {
                            Timber.e(result.exception, "Неожиданная ошибка при загрузке списка точек выгрузки бетона")
                            result.exception.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = DstPointListUiState.Error(message)
                }
            }
        }
    }
}