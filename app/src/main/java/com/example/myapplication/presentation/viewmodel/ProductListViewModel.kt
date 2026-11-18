package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Product
import com.example.domain.model.TaskRequest
import com.example.domain.usecase.CreateTaskUseCase
import com.example.domain.usecase.GetDstPointUseCase
import com.example.domain.usecase.GetProductListUseCase
import com.example.domain.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
 * Одноразовые события экрана списка продуктов.
 *
 * Доставляются через [Channel], чтобы сообщение о результате операции
 * показывалось ровно один раз и не воспроизводилось при рекомпозиции
 * или переподписке (в отличие от StateFlow).
 */
sealed interface ProductListEvent {
    /**
     * Сообщение о результате операции для Snackbar.
     *
     * @property text Текст сообщения.
     * @property isError true — сообщение об ошибке (окраска и длительность показа).
     */
    data class ShowMessage(val text: String, val isError: Boolean = false) : ProductListEvent
}

/**
 * ViewModel экрана со списком продуктов, разрешенных для отгрузки в заданную точку выгрузки.
 *
 * Помимо загрузки списка отвечает за создание заявки:
 * - [isSubmitting] — индикатор выполнения для кнопки диалога и защита от двойной отправки;
 * - [submitError] — ошибка создания, показывается внутри диалога (диалог не закрывается,
 *   введённые данные сохраняются, отправку можно повторить);
 * - [events] — одноразовые сообщения об успехе для Snackbar;
 * - [zoneName] — название точки выгрузки для диалога (вместо сырого ID зоны).
 *
 * Реагирует на глобальные события аутентификации через [authEventBus]
 * для корректного сброса состояния при выходе.
 *
 * @param getProductListUseCase UseCase для получения списка продуктов.
 * @param createTaskUseCase UseCase для создания заявки на отгрузку.
 * @param getDstPointUseCase UseCase для получения названия точки выгрузки.
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductListUseCase: GetProductListUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val getDstPointUseCase: GetDstPointUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)

    /** Флаг выполнения запроса на создание заявки: блокирует повторную отправку. */
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)

    /** Ошибка создания заявки для показа внутри диалога; null — ошибки нет. */
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    private val _zoneName = MutableStateFlow<String?>(null)

    /** Название точки выгрузки; null ещё не загружено (UI покажет фолбэк с ID). */
    val zoneName: StateFlow<String?> = _zoneName.asStateFlow()

    private val _events = Channel<ProductListEvent>(Channel.BUFFERED)

    /** Поток одноразовых событий для UI (Snackbar). */
    val events: Flow<ProductListEvent> = _events.receiveAsFlow()

    init {
        Timber.d("ProductViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                _uiState.value = ProductListUiState.Loading
                _selectedProduct.value = null
                _submitError.value = null
            }
        }
    }

    /**
     * Загружает список продуктов и (однократно) название точки выгрузки.
     *
     * При истёкшей сессии ничего не делает — TokenInterceptor уже обработал редирект на логин.
     * При других ошибках отображает человекочитаемое сообщение пользователю.
     *
     * @param zone ID зоны выгрузки.
     */
    fun loadProductList(zone: Int) {
        Timber.d("Начало загрузки списка продуктов")
        if (_zoneName.value == null) loadZoneName(zone)
        viewModelScope.launch {
            _uiState.value = ProductListUiState.Loading
            when (val result = getProductListUseCase(zone)) {
                is CustomResult.Success -> {
                    Timber.d("Список продуктов успешно загружен")
                    _uiState.value = ProductListUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    if (result.exception is SessionExpiredDomainException) {
                        Timber.w("Сессия истекла, пропуск обработки")
                        return@launch
                    }
                    Timber.e(result.exception, "Ошибка загрузки списка продуктов")
                    _uiState.value = ProductListUiState.Error(humanReadableMessage(result.exception))
                }
            }
        }
    }

    /**
     * Загружает название точки выгрузки для диалога создания заявки.
     *
     * Ошибка не критична: диалог покажет фолбэк «Зона N», поэтому
     * состояние списка продуктов не затрагивается.
     *
     * @param zone ID зоны выгрузки.
     */
    private fun loadZoneName(zone: Int) {
        viewModelScope.launch {
            when (val result = getDstPointUseCase(zone)) {
                is CustomResult.Success -> _zoneName.value = result.data.zoneName
                is CustomResult.Error ->
                    Timber.w(result.exception, "Не удалось загрузить название зоны $zone")
            }
        }
    }

    /**
     * Открывает диалог создания заявки для выбранного продукта.
     * Сбрасывает ошибку предыдущей отправки.
     *
     * @param product Продукт для заказа.
     */
    fun onProductClick(product: Product) {
        _submitError.value = null
        _selectedProduct.value = product
    }

    /** Закрывает диалог и сбрасывает ошибку отправки. */
    fun onDialogDismiss() {
        _selectedProduct.value = null
        _submitError.value = null
    }

    /**
     * Создаёт заявку на отгрузку продукта.
     *
     * Повторный вызов во время выполнения игнорируется. Результат:
     * - успех — диалог закрывается, в [events] уходит сообщение с номером заявки;
     * - ошибка — диалог остаётся открытым, текст ошибки доступен в [submitError]
     *   (введённые данные не теряются, отправку можно повторить);
     * - истёкшая сессия — диалог закрывается, редирект выполняет TokenInterceptor.
     *
     * @param zone ID зоны выгрузки.
     * @param product Заказываемый продукт.
     * @param quantity Количество продукции (валидируется в диалоге, > 0).
     * @param pallete Номер паллеты или null.
     */
    fun submitApplication(zone: Int, product: Product, quantity: Float, pallete: String?) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            _submitError.value = null
            try {
                val taskRequest = TaskRequest(
                    quantity = quantity,
                    zone = zone,
                    pallete = pallete,
                    product = product.id
                )
                when (val result = createTaskUseCase(taskRequest)) {
                    is CustomResult.Success -> {
                        Timber.d("Заявка создана: ${result.data}")
                        _selectedProduct.value = null
                        _events.send(
                            ProductListEvent.ShowMessage("Заявка №${result.data.load} создана")
                        )
                    }
                    is CustomResult.Error -> {
                        if (result.exception is SessionExpiredDomainException) {
                            Timber.w("Сессия истекла при создании заявки")
                            _selectedProduct.value = null
                            return@launch
                        }
                        Timber.e(result.exception, "Ошибка при создании заявки")
                        _submitError.value = humanReadableMessage(result.exception)
                    }
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Преобразует исключение в понятное пользователю сообщение.
     *
     * @param exception Исключение доменного слоя.
     * @return Текст для показа в UI.
     */
    private fun humanReadableMessage(exception: Throwable): String = when (exception) {
        is NetworkConnectionException -> "Нет соединения с интернетом. Проверьте подключение и повторите."
        else -> exception.message ?: "Неизвестная ошибка"
    }
}