package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.AppException
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Load
import com.example.domain.model.Terminal
import com.example.domain.usecase.DeleteLoadByIdUseCase
import com.example.domain.usecase.GetLoadQueueUseCase
import com.example.domain.usecase.UpdateLoadByIdUseCase
import com.example.domain.usecase.UpdateQueueNumLoadByIdUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import groupByTerminal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import toUpdateRequest
import javax.inject.Inject

/**
 * Состояние UI для экрана с очередью заявок на отгрузку.
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети.
 */
sealed class QueueUiState {
    /** Первичная загрузка списка. */
    data object Loading : QueueUiState()

    /** Успешно загруженная очередь, сгруппированная по терминалам. */
    data class Success(val loadsByTerminal: Map<Terminal, List<Load>>) : QueueUiState()

    /** Ошибка загрузки с сообщением для пользователя. */
    data class Error(val message: String, val isNetworkError: Boolean = false) : QueueUiState()
}

/**
 * Одноразовые события UI экрана очереди.
 *
 * Доставляются через Channel: каждое событие обрабатывается ровно один раз
 * и не переигрывается после поворота экрана или рекомпозиции.
 */
sealed interface QueueEvent {
    /**
     * Сообщение для Snackbar.
     *
     * @property text Текст для пользователя.
     * @property isError Признак ошибки (для окраски сообщения).
     */
    data class ShowMessage(val text: String, val isError: Boolean = false) : QueueEvent
}

/**
 * ViewModel экрана очереди заявок на отгрузку.
 *
 * Операции изменения, перемещения и удаления обрабатывают результат UseCase:
 * успех — сообщение и тихое обновление списка (без сброса раскрытых терминалов),
 * ошибка — сообщение без изменения списка. Реагирует на глобальные события
 * аутентификации через [authEventBus] для сброса состояния при выходе.
 *
 * @param getLoadQueueUseCase UseCase получения очереди заявок.
 * @param updateLoadByIdUseCase UseCase изменения заявки (количество продукции).
 * @param updateQueueNumLoadByIdUseCase UseCase перемещения заявки в очереди.
 * @param deleteLoadByIdUseCase UseCase удаления заявки.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class QueueViewModel @Inject constructor(
    private val getLoadQueueUseCase: GetLoadQueueUseCase,
    private val updateLoadByIdUseCase: UpdateLoadByIdUseCase,
    private val updateQueueNumLoadByIdUseCase: UpdateQueueNumLoadByIdUseCase,
    private val deleteLoadByIdUseCase: DeleteLoadByIdUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<QueueUiState>(QueueUiState.Loading)

    /** Состояние экрана очереди. */
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    /**
     * Флаг выполнения сетевой операции (изменение, перемещение или удаление).
     * Используется для блокировки повторных нажатий и оверлея в UI.
     */
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _events = Channel<QueueEvent>(Channel.BUFFERED)

    /** Поток одноразовых событий для показа сообщений в UI. */
    val events = _events.receiveAsFlow()

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
     * Загружает очередь заявок с полноэкранным индикатором загрузки.
     *
     * При истёкшей сессии ничего не делает — редирект на логин
     * уже обработан TokenInterceptor.
     */
    fun loadQueue() {
        Timber.d("Начало загрузки очереди заявок на отгрузку")
        viewModelScope.launch {
            _uiState.value = QueueUiState.Loading
            when (val result = getLoadQueueUseCase()) {
                is CustomResult.Success -> {
                    _uiState.value = QueueUiState.Success(result.data.groupByTerminal())
                }
                is CustomResult.Error -> {
                    if (result.exception is SessionExpiredDomainException) {
                        Timber.w("Сессия истекла, пропуск обработки")
                        return@launch
                    }
                    _uiState.value = QueueUiState.Error(
                        message = userMessage(result.exception, "Не удалось загрузить очередь"),
                        isNetworkError = result.exception is NetworkConnectionException
                    )
                }
            }
        }
    }

    /**
     * Изменяет количество продукции в заявке после подтверждения в UI.
     *
     * Полный запрос (терминал, продукт, подразделение) собирается из самой
     * заявки через [toUpdateRequest] и отправляется на сервер в формате
     * TerminalInfo / ProductInfo / qty / departmentid.
     * Успех: сообщение и тихое обновление списка; ошибка: сообщение,
     * список остаётся прежним.
     *
     * @param load Изменяемая заявка.
     * @param newQuantity Новое количество продукции (больше нуля).
     */
    fun updateLoad(load: Load, newQuantity: Float) {
        if (_isProcessing.value || newQuantity <= 0f) return

        val request = load.toUpdateRequest(newQuantity)
        if (request == null) {
            Timber.w("Заявка ${load.id}: нет терминала/продукта/подразделения для изменения")
            viewModelScope.launch {
                _events.send(
                    QueueEvent.ShowMessage(
                        text = "Недостаточно данных для изменения заявки №${load.id}",
                        isError = true
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                when (val result = updateLoadByIdUseCase(load.id, request)) {
                    is CustomResult.Success -> {
                        Timber.d("Заявка ${load.id} изменена: количество $newQuantity")
                        _events.send(QueueEvent.ShowMessage("Заявка №${load.id} изменена"))
                        refreshQueueSilently()
                    }
                    is CustomResult.Error -> {
                        if (result.exception is SessionExpiredDomainException) return@launch
                        Timber.w(result.exception, "Ошибка изменения заявки ${load.id}")
                        _events.send(
                            QueueEvent.ShowMessage(
                                text = userMessage(
                                    result.exception,
                                    "Не удалось изменить заявку №${load.id}"
                                ),
                                isError = true
                            )
                        )
                    }
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Перемещает заявку на новую позицию в очереди её терминала.
     *
     * Успех: сообщение и тихое обновление списка; ошибка: сообщение,
     * список остаётся прежним.
     *
     * @param load Заявка для перемещения.
     * @param newNumber Новая позиция в очереди (начиная с 1).
     */
    fun moveLoad(load: Load, newNumber: Int) {
        if (_isProcessing.value || newNumber < 1) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                when (val result = updateQueueNumLoadByIdUseCase(load.id, newNumber.toString())) {
                    is CustomResult.Success -> {
                        Timber.d("Заявка ${load.id} перемещена на позицию $newNumber")
                        _events.send(
                            QueueEvent.ShowMessage("Заявка №${load.id} перемещена на позицию $newNumber")
                        )
                        refreshQueueSilently()
                    }
                    is CustomResult.Error -> {
                        if (result.exception is SessionExpiredDomainException) return@launch
                        Timber.w(result.exception, "Ошибка перемещения заявки ${load.id}")
                        _events.send(
                            QueueEvent.ShowMessage(
                                text = userMessage(
                                    result.exception,
                                    "Не удалось переместить заявку №${load.id}"
                                ),
                                isError = true
                            )
                        )
                    }
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Удаляет заявку после подтверждения пользователем в UI.
     *
     * Успех: сообщение и тихое обновление списка; ошибка: сообщение,
     * список остаётся прежним.
     *
     * @param load Заявка для удаления.
     */
    fun deleteLoad(load: Load) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                when (val result = deleteLoadByIdUseCase(load.id)) {
                    is CustomResult.Success -> {
                        Timber.d("Заявка ${load.id} удалена")
                        _events.send(QueueEvent.ShowMessage("Заявка №${load.id} удалена"))
                        refreshQueueSilently()
                    }
                    is CustomResult.Error -> {
                        if (result.exception is SessionExpiredDomainException) return@launch
                        Timber.w(result.exception, "Ошибка удаления заявки ${load.id}")
                        _events.send(
                            QueueEvent.ShowMessage(
                                text = userMessage(
                                    result.exception,
                                    "Не удалось удалить заявку №${load.id}"
                                ),
                                isError = true
                            )
                        )
                    }
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Обновляет очередь без полноэкранного индикатора загрузки.
     *
     * Сохраняет текущий список на экране (и раскрытые терминалы):
     * данные заменяются только при успешном ответе, при ошибке
     * показывается сообщение, а список остаётся прежним.
     */
    private suspend fun refreshQueueSilently() {
        when (val result = getLoadQueueUseCase()) {
            is CustomResult.Success -> {
                _uiState.value = QueueUiState.Success(result.data.groupByTerminal())
            }
            is CustomResult.Error -> {
                if (result.exception is SessionExpiredDomainException) return
                _events.send(
                    QueueEvent.ShowMessage(
                        text = userMessage(result.exception, "Не удалось обновить очередь"),
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Преобразует исключение в сообщение, понятное пользователю.
     *
     * @param exception Ошибка операции.
     * @param fallback Текст, если у исключения нет сообщения.
     * @return Сообщение для отображения в UI.
     */
    private fun userMessage(exception: AppException, fallback: String): String =
        when (exception) {
            is NetworkConnectionException -> "Нет соединения с интернетом"
            else -> exception.message ?: fallback
        }
}