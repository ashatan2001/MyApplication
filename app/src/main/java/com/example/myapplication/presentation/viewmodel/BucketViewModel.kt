package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Bucket
import com.example.domain.model.Load
import com.example.domain.model.Terminal
import com.example.domain.usecase.GetBucketListUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Состояние UI для экрана с очередью заявок на отгрузку.
 *
 * Используется [StateFlow] для реактивного обновления UI:
 * - [Loading] — отображается спиннер во время загрузки
 * - [Success] — отображается очередь заявок на отгрузку
 * - [Error] — отображается сообщение об ошибке с возможностью retry
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети
 */
sealed class BucketUiState {
    data object Loading : BucketUiState()
    data class Success(val bucketList: List<Bucket>) : BucketUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : BucketUiState()
}

/**
 * ViewModel экрана со списком бетоновозных телег.
 * Реагирует на глобальные события аутентификации через [authEventBus]
 * для корректного сброса состояния при выходе.»
 *
 * @param getBucketListUseCase UseCase для получения списка бетоновозных телег.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class BucketViewModel @Inject constructor(
    private val getBucketListUseCase: GetBucketListUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<BucketUiState>(BucketUiState.Loading)
    val uiState: StateFlow<BucketUiState> = _uiState.asStateFlow()

    init {
        Timber.d("BucketViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = BucketUiState.Loading
            }
        }
    }

    /**
     * Загружает список бетоновозных телег из репозитория.
     *
     * При истёкшей сессии ничего не делает — TokenInterceptor уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadBucket() {
        Timber.d("Начало загрузки списка бетоновозных телег")
        viewModelScope.launch {
            _uiState.value = BucketUiState.Loading
            when (val result = getBucketListUseCase()) {
                is CustomResult.Success -> {
                    Timber.d("Список бетоновозных телег успешно загружен")
                    _uiState.value = BucketUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is NetworkConnectionException -> {
                            Timber.w("Ошибка сети при загрузке списка бетоновозных телег")
                            "Нет соединения с интернетом"
                        }
                        else -> {
                            Timber.e(result.exception, "Неожиданная ошибка при загрузке списка бетоновозных телег")
                            result.exception.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = BucketUiState.Error(message)
                }
            }
        }
    }
}