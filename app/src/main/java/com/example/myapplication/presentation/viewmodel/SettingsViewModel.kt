package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.ServerApiException
import com.example.domain.usecase.ObserveServerUrlUseCase
import com.example.domain.usecase.ResetServerUrlUseCase
import com.example.domain.usecase.SaveServerUrlUseCase
import com.example.domain.usecase.TestConnectionUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Состояния экрана настроек сервера.
 */
sealed class SettingsUiState {
    /** Загрузка текущих настроек */
    data object Loading : SettingsUiState()

    /** Успешная загрузка с текущим URL */
    data class Success(val currentUrl: String) : SettingsUiState()

    /** Процесс сохранения */
    data object Saving : SettingsUiState()

    /** Процесс проверки подключения */
    data object Testing : SettingsUiState()

    /** Ошибка с сообщением */
    data class Error(val message: String) : SettingsUiState()
}

/**
 * Статус подключения к серверу для UI-индикатора.
 */
sealed class ServerConnectionStatus {
    /** Подключение не проверялось */
    data object NotChecked : ServerConnectionStatus()

    /** Проверка выполняется */
    data object Checking : ServerConnectionStatus()

    /** Сервер доступен */
    data object Connected : ServerConnectionStatus()

    /** Сервер недоступен */
    data class Disconnected(val error: String) : ServerConnectionStatus()
}

/**
 * Одноразовые события для навигации из экрана настроек.
 */
sealed class SettingsEvent {
    /** URL успешно сохранен, можно переходить к авторизации */
    data object NavigateToAuth : SettingsEvent()
}

/**
 * ViewModel для управления состоянием экрана настроек сервера.
 * Использует отдельные UseCase для каждой операции, соблюдая принципы чистой архитектуры.
 *
 * @param observeServerUrlUseCase UseCase для наблюдения за URL.
 * @param saveServerUrlUseCase UseCase для сохранения URL.
 * @param resetServerUrlUseCase UseCase для сброса URL.
 * @param testConnectionUseCase UseCase для проверки подключения.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeServerUrlUseCase: ObserveServerUrlUseCase,
    private val saveServerUrlUseCase: SaveServerUrlUseCase,
    private val resetServerUrlUseCase: ResetServerUrlUseCase,
    private val testConnectionUseCase: TestConnectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ServerConnectionStatus>(ServerConnectionStatus.NotChecked)
    val connectionStatus: StateFlow<ServerConnectionStatus> = _connectionStatus.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadCurrentSettings()
    }

    /**
     * Загружает текущие настройки сервера из репозитория.
     */
    private fun loadCurrentSettings() {
        viewModelScope.launch {
            observeServerUrlUseCase().collect { url ->
                _uiState.value = SettingsUiState.Success(url)
            }
        }
    }

    /**
     * Сохраняет новый URL сервера.
     * После успешного сохранения отправляет событие для навигации на экран авторизации.
     *
     * @param url Новый адрес сервера.
     */
    fun saveUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Saving
            try {
                saveServerUrlUseCase(url)
                // Отправляем событие для навигации на экран авторизации
                _events.send(SettingsEvent.NavigateToAuth)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    /**
     * Сбрасывает URL сервера к значению по умолчанию.
     */
    fun resetToDefault() {
        viewModelScope.launch {
            try {
                resetServerUrlUseCase()
                // StateFlow из loadCurrentSettings сам обновит UI
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Ошибка сброса")
            }
        }
    }

    /**
     * Тестирует подключение к указанному URL через [TestConnectionUseCase].
     *
     * @param url URL для проверки.
     */
    fun testConnection(url: String) {
        viewModelScope.launch {
            Timber.d("Начало проверки подключения к: $url")
            _connectionStatus.value = ServerConnectionStatus.Checking

            val result = testConnectionUseCase(url)

            when (result) {
                is CustomResult.Success -> {
                    Timber.d("Подключение успешно установлено")
                    _connectionStatus.value = ServerConnectionStatus.Connected
                }
                is CustomResult.Error -> {
                    val e = result.exception
                    Timber.e(e, "Ошибка при проверке подключения")

                    val errorMessage = when (e) {
                        is NetworkConnectionException -> "Нет подключения к интернету или хост не найден"
                        is ServerApiException -> e.message ?: "Сервер вернул ошибку"
                        else -> e.message ?: "Неизвестная ошибка"
                    }

                    _connectionStatus.value = ServerConnectionStatus.Disconnected(errorMessage)
                }
            }
        }
    }
}