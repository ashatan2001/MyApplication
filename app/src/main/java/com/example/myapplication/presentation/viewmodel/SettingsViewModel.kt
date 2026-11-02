package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.ManageServerSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Возможные состояния экрана настроек.
 */
sealed class SettingsUiState {
    /** Идет первоначальная загрузка текущего значения */
    data object Loading : SettingsUiState()

    /** Успешная загрузка или обновление, содержит текущий URL */
    data class Success(val currentUrl: String) : SettingsUiState()

    /** Идет процесс сохранения или сброса */
    data object Saving : SettingsUiState()

    /** Произошла ошибка, содержит сообщение для пользователя */
    data class Error(val message: String) : SettingsUiState()
}

/**
 * ViewModel экрана настроек сервера.
 * Управляет состоянием UI и делегирует бизнес-логику в ManageServerSettingsUseCase.
 *
 * @param manageServerSettingsUseCase UseCase для управления настройками сервера.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageServerSettingsUseCase: ManageServerSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Наблюдаем за изменениями URL через UseCase
        viewModelScope.launch {
            manageServerSettingsUseCase.serverUrl.collect { url ->
                _uiState.value = SettingsUiState.Success(url)
            }
        }
    }

    /**
     * Валидирует и сохраняет новый URL сервера.
     *
     * @param inputUrl Строка, введенная пользователем.
     */
    fun saveUrl(inputUrl: String) {
        val trimmedUrl = inputUrl.trim()

        // Базовая валидация формата на уровне UI для мгновенной обратной связи
        if (trimmedUrl.isBlank()) {
            _uiState.value = SettingsUiState.Error("URL не может быть пустым")
            return
        }

        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            _uiState.value = SettingsUiState.Error("URL должен начинаться с http:// или https://")
            return
        }

        viewModelScope.launch {
            _uiState.value = SettingsUiState.Saving
            // Вызов UseCase как функции (благодаря operator fun invoke)
            val result = manageServerSettingsUseCase(trimmedUrl)

            result.fold(
                onSuccess = {
                    // Состояние Success обновится автоматически через collect в init
                },
                onFailure = { exception ->
                    _uiState.value = SettingsUiState.Error("Ошибка сохранения: ${exception.message}")
                }
            )
        }
    }

    /**
     * Сбрасывает URL к значению по умолчанию через UseCase.
     */
    fun resetToDefault() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Saving
            val result = manageServerSettingsUseCase.resetToDefault()

            result.fold(
                onSuccess = {
                    // Состояние Success обновится автоматически через collect в init
                },
                onFailure = { exception ->
                    _uiState.value = SettingsUiState.Error("Ошибка сброса: ${exception.message}")
                }
            )
        }
    }
}