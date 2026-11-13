package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Bucket
import com.example.domain.usecase.GetBucketListUseCase
import com.example.domain.util.CustomResult
import com.example.domain.util.SmartPoller
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
 *
 * Механику периодического опроса (интервалы, backoff, фатальные ошибки, дедупликация)
 * инкапсулирует [SmartPoller]; на ViewModel остаётся только маппинг результатов
 * в [BucketUiState] и реакция на события аутентификации.
 *
 * @param getBucketListUseCase Use-case получения списка телег.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class BucketViewModel @Inject constructor(
    private val getBucketListUseCase: GetBucketListUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<BucketUiState>(BucketUiState.Loading)
    val uiState: StateFlow<BucketUiState> = _uiState.asStateFlow()

    /** Поллер телег: 5с в норме, до 30с при затяжных ошибках сети. */
    private val bucketPoller = SmartPoller(
        fetch = { getBucketListUseCase() }
    )

    init {
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                bucketPoller.stop()
                _uiState.value = BucketUiState.Loading
            }
        }
    }

    /**
     * Запускает мониторинг телег. Идемпотентен.
     * Вызывается экраном при переходе в STARTED (LifecycleStartEffect).
     */
    fun startMonitoring() {
        bucketPoller.start(viewModelScope, ::handleResult)
    }

    /**
     * Останавливает мониторинг: уход с экрана, сворачивание приложения.
     */
    fun stopMonitoring() {
        bucketPoller.stop()
    }

    /**
     * Первичная загрузка со спиннером: первый вход и кнопка «Повторить».
     */
    fun loadBucketList() {
        viewModelScope.launch {
            _uiState.value = BucketUiState.Loading
            handleResult(getBucketListUseCase())
        }
    }

    /**
     * Маппит очередной результат опроса в состояние UI.
     *
     * Обновление «тихое»: без сброса в Loading, поэтому спиннер не мигает,
     * раскрытые карточки и позиция скролла сохраняются. StateFlow сам подавляет
     * структурно равные списки — UI обновляется только при реальных изменениях.
     * Ошибка сети не затирает уже показанные данные экраном ошибки.
     *
     * @param result Результат очередной попытки получения списка телег.
     */
    private fun handleResult(result: CustomResult<List<Bucket>>) {
        when (result) {
            is CustomResult.Success -> {
                _uiState.value = BucketUiState.Success(result.data)
            }
            is CustomResult.Error -> {
                if (result.exception is SessionExpiredDomainException) {
                    // TokenInterceptor уже увёл на логин; поллер и сам завершится, stop — страховка
                    bucketPoller.stop()
                    return
                }
                Timber.w(result.exception, "Ошибка обновления списка телег")
                if (_uiState.value !is BucketUiState.Success) {
                    val message = if (result.exception is NetworkConnectionException) {
                        "Нет соединения с интернетом"
                    } else {
                        result.exception.message ?: "Неизвестная ошибка"
                    }
                    _uiState.value = BucketUiState.Error(message)
                }
            }
        }
    }
}