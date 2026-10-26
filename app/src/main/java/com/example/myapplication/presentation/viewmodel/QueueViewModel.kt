package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Load
import com.example.domain.model.Terminal
import com.example.domain.usecase.DeleteLoadByIdUseCase
import com.example.domain.usecase.GetLoadQueueUseCase
import com.example.domain.usecase.UpdateLoadByIdUseCase
import com.example.domain.util.CustomResult
import com.example.domain.util.groupByTerminal
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
sealed class QueueUiState {
    data object Loading : QueueUiState()
    data class Success(val loadsByTerminal: Map<Terminal, List<Load>>) : QueueUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : QueueUiState()
}

/**
 * ViewModel экрана с очередью заявок на отгрузку. Реагирует на глобальные события аутентификации
 * через [authEventBus] для корректного сброса состояния при выходе.»
 *
 * @param getLoadQueueUseCase UseCase для получения очереди заявок на отгрузку.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class QueueViewModel @Inject constructor(
    private val getLoadQueueUseCase: GetLoadQueueUseCase,
    private val updateLoadByIdUseCase: UpdateLoadByIdUseCase,
    private val deleteLoadByIdUseCase: DeleteLoadByIdUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<QueueUiState>(QueueUiState.Loading)
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    /**
     * Флаг, указывающий на выполнение сетевой операции (изменение или удаление).
     * Используется для блокировки повторных нажатий в UI.
     */
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    init {
        Timber.d("QueueViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = QueueUiState.Loading
            }
        }
    }

    /**
     * Загружает очередь заявок на отгрузку из репозитория.
     *
     * При истёкшей сессии ничего не делает — [TokenInterceptor] уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadQueue() {
        Timber.d("Начало загрузки очереди заявок на отгрузку")
        viewModelScope.launch {
            _uiState.value = QueueUiState.Loading
            when (val result = getLoadQueueUseCase()) {
                is CustomResult.Success -> {
                    Timber.d("Очередь заявок на отгрузку успешно загружена")
                    _uiState.value = QueueUiState.Success(result.data.groupByTerminal())
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            // TokenInterceptor уже обработал редирект на логин
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is NetworkConnectionException -> {
                            Timber.w("Ошибка сети при загрузке очереди заявок на отгрузку")
                            "Нет соединения с интернетом"
                        }
                        else -> {
                            Timber.e(result.exception, "Неожиданная ошибка при загрузке очереди заявок на отгрузку")
                            result.exception.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = QueueUiState.Error(message)
                }
            }
        }
    }

    /**
     * Инициирует процесс изменения выбранной заявки.
     *
     * @param load Заявка для изменения.
     */
    fun updateLoad(load: Load) {
        Timber.d("Инициализация изменения заявки: ${load.id}")
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // TODO: Вызов UseCase для редактирования, например: updateLoadUseCase(load)
                // После успеха: loadQueue()
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Инициирует процесс удаления выбранной заявки.
     *
     * @param load Заявка для удаления.
     */
    fun deleteLoad(load: Load) {
        Timber.d("Инициализация удаления заявки: ${load.id}")
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                deleteLoadByIdUseCase(load.id.toString())
                loadQueue()
            } finally {
                _isProcessing.value = false
            }
        }
    }
}