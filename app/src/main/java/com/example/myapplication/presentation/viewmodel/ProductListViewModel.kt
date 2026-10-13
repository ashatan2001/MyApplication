package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Product
import com.example.domain.usecase.GetProductListUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Состояние UI для экрана со списком продуктов, разрешенных для отгрузки в заданную точку выгрузки.
 *
 * Используется [StateFlow] для реактивного обновления UI:
 * - [Loading] — отображается спиннер во время загрузки
 * - [Success] — отображается список продуктов
 * - [Error] — отображается сообщение об ошибке с возможностью retry
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети
 */
sealed class ProductListUiState {
    data object Loading : ProductListUiState()
    data class Success(val productList: List<Product>) : ProductListUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : ProductListUiState()
}

/**
 * ViewModel экрана со списком продуктов, разрешенных для отгрузки в заданную точку выгрузки.
 * Реагирует на глобальные события аутентификации через [authEventBus]
 * для корректного сброса состояния при выходе.»
 *
 * @param getProductListUseCase UseCase для получения списка продуктов,
 *                              разрешенных для отгрузки в заданную точку выгрузки.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductListUseCase: GetProductListUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        Timber.d("ProductViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = ProductListUiState.Loading
            }
        }
    }

    /**
     * Загружает данные продукта из репозитория.
     *
     * При истёкшей сессии ничего не делает — [TokenInterceptor] уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadProductList(zone: Int) {
        Timber.d("Начало загрузки списка продуктов")
        viewModelScope.launch {
            _uiState.value = ProductListUiState.Loading
            when (val result = getProductListUseCase(zone)) {
                is CustomResult.Success -> {
                    Timber.d("Список продуктов успешно загружен")
                    _uiState.value = ProductListUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            // TokenInterceptor уже обработал редирект на логин
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is NetworkConnectionException -> {
                            Timber.w("Ошибка сети при загрузке списка продуктов")
                            "Нет соединения с интернетом"
                        }
                        else -> {
                            Timber.e(result.exception, "Неожиданная ошибка при загрузке списка продуктов")
                            result.exception.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = ProductListUiState.Error(message)
                }
            }
        }
    }

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    fun onProductClick(product: Product) {
        _selectedProduct.value = product
    }

    fun onDialogDismiss() {
        _selectedProduct.value = null
    }

    fun submitApplication(product: Product, quantity: Int) {
        viewModelScope.launch {
            Timber.d("Отправка заявки: продукт=${product.name}, кол-во=$quantity")
            // TODO: Вызов UseCase: createApplicationUseCase(product.id, quantity, comment)

            // Закрываем диалог после успешной отправки
            _selectedProduct.value = null
        }
    }
}