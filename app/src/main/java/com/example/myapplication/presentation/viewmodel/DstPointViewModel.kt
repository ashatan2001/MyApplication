package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.DstPoint
import com.example.domain.usecase.GetDstPointUseCase
import com.example.domain.util.CustomResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Состояние UI для экрана профиля точки выгрузки бетона.
 *
 * Используется [StateFlow] для реактивного обновления UI:
 * - [Loading] — отображается спиннер во время загрузки
 * - [Success] — отображаются данные точки выгрузки бетона
 * - [Error] — отображается сообщение об ошибке с возможностью retry
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети
 */
sealed class DstPointUiState {
    data object Loading : DstPointUiState()
    data class Success(val dstPoint: DstPoint) : DstPointUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : DstPointUiState()
}

/**
 * ViewModel экрана точки выгрузки бетона. Реагирует на глобальные события аутентификации
 * через [authEventBus] для корректного сброса состояния при выходе.»
 *
 * @param getDstPointUseCase UseCase для получения информации о точке выгрузки бетона.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
class DstPointViewModel @Inject constructor(
    private val getDstPointUseCase: GetDstPointUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<DstPointUiState>(DstPointUiState.Loading)
    val uiState: StateFlow<DstPointUiState> = _uiState.asStateFlow()

    init {
        Timber.d("DstPointViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = DstPointUiState.Loading
            }
        }
    }

    /**
     * Загружает данные точки выгрузки бетона из репозитория.
     *
     * При истёкшей сессии ничего не делает — [TokenInterceptor] уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadDstPointData() {
        Timber.d("Начало загрузки данных точки выгрузки бетона")
        viewModelScope.launch {
            _uiState.value = DstPointUiState.Loading
            when (val result = getDstPointUseCase(zoneId)) {
                is CustomResult.Success -> {
                    Timber.d("Данные точки выгрузки бетона успешно загружены")
                    _uiState.value = DstPointUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            // TokenInterceptor уже обработал редирект на логин
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is UserNotFoundException -> {
                            Timber.w("Точка выгрузки бетона не найдена")
                            "Точка выгрузки бетона не найдена"
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
                    _uiState.value = DstPointUiState.Error(message)
                }
            }
        }
    }
}