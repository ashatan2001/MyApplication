package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Bucket
import com.example.domain.model.DstPoint
import com.example.domain.usecase.GetBucketListUseCase
import com.example.domain.usecase.GetDstPointListUseCase
import com.example.domain.util.CustomResult
import com.example.domain.util.SmartPoller
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Состояние UI для экрана со списком точек выгрузки бетона.
 *
 * Используется [StateFlow] для реактивного обновления UI:
 * - [Loading] — отображается спиннер во время загрузки
 * - [Success] — отображается список точек выгрузки бетона
 * - [Error] — отображается сообщение об ошибке с возможностью retry
 *
 * @property isNetworkError Флаг для показа специфичного UI при ошибках сети
 */
sealed class DstPointListUiState {
    data object Loading : DstPointListUiState()
    data class Success(val dstPointList: List<DstPoint>) : DstPointListUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : DstPointListUiState()
}

/**
 * Одноразовые события экрана списка точек выгрузки.
 *
 * Доставляются через [Channel], чтобы уведомление о прибытии телеги
 * показывалось ровно один раз и не воспроизводилось при рекомпозиции
 * или переподписке (в отличие от StateFlow).
 */
sealed interface DstPointListEvent {
    /**
     * Телега прибыла на точку выгрузки.
     *
     * @property zoneId Код зоны, куда прибыла телега (ключ строки для подсветки).
     * @property bucketName Наименование прибывшей телеги (для текста уведомления).
     * @property zoneName Наименование точки выгрузки (для текста уведомления).
     */
    data class BucketArrived(
        val zoneId: Int,
        val bucketName: String,
        val zoneName: String
    ) : DstPointListEvent
}

/**
 * ViewModel экрана со списком точек выгрузки бетона.
 *
 * Помимо загрузки списка точек отвечает за мониторинг прибытия телег:
 * механику периодического опроса (интервалы, backoff, фатальные ошибки, дедупликация)
 * инкапсулирует [SmartPoller], а ViewModel маппит результаты опроса в
 * [arrivedBuckets] (телега на точке -> кнопка «Выгрузить») и одноразовые
 * события [DstPointListEvent.BucketArrived] (Snackbar + временная подсветка строки).
 *
 * Реагирует на глобальные события аутентификации через [authEventBus]
 * для корректного сброса состояния при выходе.
 *
 * @param getDstPointListUseCase UseCase для получения списка точек выгрузки бетона.
 * @param getBucketListUseCase UseCase для получения списка бетоновозных телег (мониторинг).
 * @param authEventBus Шина событий для отслеживания выхода из системы.
 */
@HiltViewModel
class DstPointListViewModel @Inject constructor(
    private val getDstPointListUseCase: GetDstPointListUseCase,
    private val getBucketListUseCase: GetBucketListUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<DstPointListUiState>(DstPointListUiState.Loading)
    val uiState: StateFlow<DstPointListUiState> = _uiState.asStateFlow()

    private val _arrivedBuckets = MutableStateFlow<Map<Int, Bucket>>(emptyMap())

    /**
     * Телеги, находящиеся на точках выгрузки: ключ — zoneId точки, значение — телега.
     *
     * Наличие записи означает «телега на точке» и включает кнопку «Выгрузить»
     * в соответствующей строке; отсутствие записи — кнопка скрыта.
     * Если на одной точке несколько телег, выбирается прибывшая раньше всех.
     */
    val arrivedBuckets: StateFlow<Map<Int, Bucket>> = _arrivedBuckets.asStateFlow()

    private val _events = Channel<DstPointListEvent>(Channel.BUFFERED)

    /** Поток одноразовых событий для UI (уведомление о прибытии телеги). */
    val events: Flow<DstPointListEvent> = _events.receiveAsFlow()

    /** Поллер телег: 5с в норме, до 30с при затяжных ошибках сети (конфигурация по умолчанию). */
    private val bucketPoller = SmartPoller(
        fetch = { getBucketListUseCase() }
    )

    /**
     * Слепок предыдущего успешного опроса: zoneId -> коды телег на точке.
     * Служит базой для детекции именно новых прибытий, а не простого присутствия.
     */
    private var lastArrivedIdsByZone: Map<Int, Set<Int>> = emptyMap()

    /**
     * Признак полученного базового слепка: самый первый успешный ответ лишь
     * заполняет состояние (кнопки), но не порождает уведомлений — телеги,
     * уже стоявшие на точках к моменту открытия экрана, не считаются «прибывшими».
     */
    private var isArrivalBaselineInitialized = false

    init {
        Timber.d("DstPointViewModel инициализирован")
        viewModelScope.launch {
            authEventBus.logoutEvents.collect {
                Timber.d("Получено событие выхода, сброс состояния на Loading")
                bucketPoller.stop()
                resetArrivalTracking()
                _uiState.value = DstPointListUiState.Loading
            }
        }
    }

    /**
     * Загружает список точек выгрузки бетона из репозитория.
     *
     * При истёкшей сессии ничего не делает — [TokenInterceptor] уже обработал редирект на логин.
     * При других ошибках отображает сообщение пользователю.
     */
    fun loadDstPointList() {
        Timber.d("Начало загрузки списка точек выгрузки бетона")
        viewModelScope.launch {
            _uiState.value = DstPointListUiState.Loading
            when (val result = getDstPointListUseCase()) {
                is CustomResult.Success -> {
                    Timber.d("Список точек выгрузки бетона успешно загружен")
                    _uiState.value = DstPointListUiState.Success(result.data)
                }
                is CustomResult.Error -> {
                    val message = when (result.exception) {
                        is SessionExpiredDomainException -> {
                            // TokenInterceptor уже обработал редирект на логин
                            Timber.w("Сессия истекла, пропуск обработки")
                            return@launch
                        }
                        is NetworkConnectionException -> {
                            Timber.w("Ошибка сети при загрузке списка точек выгрузки бетона")
                            "Нет соединения с интернетом"
                        }
                        else -> {
                            Timber.e(result.exception, "Неожиданная ошибка при загрузке списка точек выгрузки бетона")
                            result.exception.message ?: "Неизвестная ошибка"
                        }
                    }
                    _uiState.value = DstPointListUiState.Error(message)
                }
            }
        }
    }

    /**
     * Запускает мониторинг прибытия телег на точки выгрузки. Идемпотентен.
     * Вызывается экраном при переходе в STARTED (LifecycleStartEffect).
     */
    fun startBucketMonitoring() {
        bucketPoller.start(viewModelScope, ::handleBucketResult)
    }

    /**
     * Останавливает мониторинг телег: уход с экрана, сворачивание приложения.
     *
     * Слепок прибытий намеренно сохраняется: после возврата на экран первый же
     * опрос сравнится с ним, и о телегах, прибывших за время отсутствия,
     * придут уведомления (ViewModel переживает переключение вкладок MainShell).
     */
    fun stopBucketMonitoring() {
        bucketPoller.stop()
    }

    /**
     * Маппит очередной результат опроса телег в состояние мониторинга.
     *
     * Обновление «тихое»: ошибки сети не затирают ни список точек, ни уже
     * показанные кнопки — SmartPoller сам наращивает паузу (backoff) и
     * повторяет запрос. При истёкшей сессии опрос прекращается.
     *
     * @param result Результат очередной попытки получения списка телег.
     */
    private fun handleBucketResult(result: CustomResult<List<Bucket>>) {
        when (result) {
            is CustomResult.Success -> updateArrivedBuckets(result.data)
            is CustomResult.Error -> {
                if (result.exception is SessionExpiredDomainException) {
                    // TokenInterceptor уже увёл на логин; поллер и сам завершится, stop — страховка
                    bucketPoller.stop()
                    return
                }
                Timber.w(result.exception, "Ошибка обновления местоположения телег")
            }
        }
    }

    /**
     * Обновляет карту «точка -> телега» и детектирует новые прибытия.
     *
     * Прибытием считается появление на точке телеги, которой не было там
     * в предыдущем успешном опросе (сравнение по кодам телег, а не по объектам:
     * смена временных отметок уже стоящей телеги не должна порождать событие).
     * Для каждого нового прибытия отправляется одноразовое событие
     * [DstPointListEvent.BucketArrived] — Snackbar и подсветка строки на UI.
     *
     * @param buckets Полный список телег, полученный очередным опросом.
     */
    private fun updateArrivedBuckets(buckets: List<Bucket>) {
        val atPoints = buckets.filter { it.isAtDstPoint() }
        val idsByZone: Map<Int, Set<Int>> = atPoints
            .groupBy { it.zoneNumber }
            .mapValues { (_, zoneBuckets) -> zoneBuckets.map { it.id }.toSet() }

        if (isArrivalBaselineInitialized) {
            idsByZone.forEach { (zoneId, ids) ->
                val knownIds = lastArrivedIdsByZone[zoneId].orEmpty()
                (ids - knownIds).forEach { newId ->
                    val bucket = atPoints.first { it.zoneNumber == zoneId && it.id == newId }
                    Timber.d("Телега ${bucket.name} прибыла на точку $zoneId")
                    // Буфер канала (64) заведомо больше числа одновременных прибытий;
                    // trySend не приостанавливает обработчик результатов поллера.
                    _events.trySend(
                        DstPointListEvent.BucketArrived(
                            zoneId = zoneId,
                            bucketName = bucket.name,
                            zoneName = resolveZoneName(zoneId)
                        )
                    )
                }
            }
        }
        isArrivalBaselineInitialized = true
        lastArrivedIdsByZone = idsByZone

        // Для кнопки «Выгрузить» — одна телега на точку: прибывшая раньше всех
        // (стабильный детерминированный выбор, не зависящий от порядка ответа сервера).
        _arrivedBuckets.value = atPoints
            .groupBy { it.zoneNumber }
            .mapValues { (_, zoneBuckets) ->
                zoneBuckets.minWith(
                    compareBy<Bucket, LocalDateTime?>(nullsLast()) { it.arrivalDate }
                        .thenBy { it.id }
                )
            }
    }

    /**
     * Признак «телега прибыла на точку выгрузки и ещё не завершила разгрузку».
     *
     * Критерии:
     * - есть активная заявка на отгрузку ([Bucket.loadId] != 0);
     * - текущий участок совпадает с участком назначения — телега физически
     *   находится в зоне своей точки выгрузки, а не проезжает мимо;
     * - разгрузка ещё не завершена ([Bucket.departDate] == null) — после
     *   завершения кнопка скрывается, даже если телега ещё стоит в зоне.
     */
    private fun Bucket.isAtDstPoint(): Boolean =
        loadId != 0L && zoneNumber == dstZone && departDate == null

    /**
     * Возвращает наименование точки выгрузки для текста уведомления.
     *
     * Если список точек ещё не загружен, использует фолбэк с кодом зоны.
     *
     * @param zoneId Код зоны выгрузки.
     * @return Наименование зоны либо «Зона N».
     */
    private fun resolveZoneName(zoneId: Int): String =
        (_uiState.value as? DstPointListUiState.Success)
            ?.dstPointList?.firstOrNull { it.zoneId == zoneId }?.zoneName
            ?: "Зона $zoneId"

    /**
     * Полностью сбрасывает отслеживание прибытий (выход из системы):
     * карта телег, слепок и признак базового опроса.
     */
    private fun resetArrivalTracking() {
        lastArrivedIdsByZone = emptyMap()
        isArrivalBaselineInitialized = false
        _arrivedBuckets.value = emptyMap()
    }
}