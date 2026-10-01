package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.exception.PersonNotFoundException
import com.example.domain.model.Person
import com.example.domain.usecase.GetPersonInfoUseCase
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
sealed class PersonUiState {
    data object Loading : PersonUiState()
    data class Success(val person: Person) : PersonUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : PersonUiState()
}

/**
 * ViewModel экрана профиля. Реагирует на глобальные события аутентификации
 * через [authEventBus] для корректного сброса состояния при выходе.»
 *
 * @param getPersonInfoUseCase UseCase для получения информации о пользователе.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class PersonViewModel @Inject constructor(
    private val getPersonInfoUseCase: GetPersonInfoUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<PersonUiState>(PersonUiState.Loading)
    val uiState: StateFlow<PersonUiState> = _uiState.asStateFlow()

    init {
        Timber.d("PersonViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = PersonUiState.Loading
            }
        }
    }

    /**
     * Загружает данные пользователя из репозитория.
     *
     * При истёкшей сессии ничего не делает — [TokenInterceptor] уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadPersonData() {
        Timber.d("Начало загрузки данных пользователя")
        viewModelScope.launch {
            _uiState.value = PersonUiState.Loading
            when (val result = getPersonInfoUseCase()) {
                is CustomResult.Success -> {
                    Timber.d("Данные пользователя успешно загружены")
                    _uiState.value = PersonUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            // TokenInterceptor уже обработал редирект на логин
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is PersonNotFoundException -> {
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
                    _uiState.value = PersonUiState.Error(message)
                }
            }
        }
    }
}