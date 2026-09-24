package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.*
import com.example.domain.model.UserModel
import com.example.domain.usecase.GetUserInfoUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Состояние UI для экрана профиля пользователя.
 *
 * Используется [StateFlow] для реактивного обновления UI:
 * - [Loading] — отображается спиннер во время загрузки
 * - [Success] — отображаются данные пользователя
 * - [Error] — отображается сообщение об ошибке с возможностью retry
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети
 */
sealed class UserUiState {
    data object Loading : UserUiState()
    data class Success(val user: UserModel) : UserUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : UserUiState()
}

/**
 * ViewModel экрана профиля. Реагирует на глобальные события аутентификации
 * через [authEventBus] для корректного сброса состояния при выходе.»
 *
 * @param getUserInfoUseCase UseCase для получения информации о пользователе.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        Timber.d("UserViewModel initialized")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = UserUiState.Loading
            }
        }
    }


    fun loadUserData() {
        Timber.d("Начало загрузки данных пользователя")
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            when (val result = getUserInfoUseCase()) {
                is CustomResult.Success -> {
                    Timber.d("Данные пользователя успешно загружены")
                    _uiState.value = UserUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is UserNotFoundException -> {
                            Timber.w("Пользователь не найден")
                            "Пользователь не найден"
                        }
                        is NetworkConnectionException -> {
                            Timber.w("Ошибка сети при загрузке данных пользователя")
                            "Нет соединения с интернетом"
                        }
                        else -> {
                            Timber.e(result.exception, "Неожиданная ошибка при загрузке данных пользователя")
                            result.exception.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = UserUiState.Error(message)
                }
            }
        }
    }
}