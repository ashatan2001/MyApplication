package com.example.domain.util

import com.example.domain.exception.AppException
import com.example.domain.exception.SessionExpiredDomainException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Настройки опроса сервера.
 *
 * @property baseIntervalMs Базовая пауза между запросами при нормальной работе, мс.
 * @property maxIntervalMs Верхняя граница паузы при нарастающем backoff, мс.
 * @property backoffMultiplier Множитель паузы после каждой подряд неудачной попытки (>= 1.0).
 * @property jitterRatio Случайный разброс паузы в долях от её величины, 0.0–0.9.
 *   0.0 — детерминированные паузы; 0.1–0.2 полезно, когда сервер опрашивает парк устройств,
 *   чтобы запросы не выстраивались в синхронные «пики».
 */
data class SmartPollerConfig(
    val baseIntervalMs: Long = 5_000L,
    val maxIntervalMs: Long = 30_000L,
    val backoffMultiplier: Double = 2.0,
    val jitterRatio: Double = 0.0
) {
    init {
        require(baseIntervalMs > 0) { "baseIntervalMs должен быть положительным" }
        require(maxIntervalMs >= baseIntervalMs) { "maxIntervalMs не может быть меньше baseIntervalMs" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier должен быть >= 1.0" }
        require(jitterRatio in 0.0..0.9) { "jitterRatio должен быть в диапазоне 0.0..0.9" }
    }
}

/**
 * «Умный» периодический опрос произвольного suspend-источника данных.
 *
 * Поведение:
 * - запросы строго последовательные: пауза отсчитывается после завершения предыдущего,
 *   поэтому при медленной сети они не накапливаются;
 * - при ошибках пауза растёт экспоненциально до [SmartPollerConfig.maxIntervalMs],
 *   после первого успеха сбрасывается к базовой;
 * - фатальные ошибки (по умолчанию — истёкшая сессия) завершают опрос:
 *   результат эмитится один раз, затем поток completes;
 * - подряд идущие структурно равные результаты подавляются ([distinctUntilChanged]).
 *
 * Класс не знает об Android и UI: жизненный цикл задаёт вызывающая сторона
 * (start/stop из ViewModel по событиям экрана). Все методы вызываются из одного потока
 * (в приложении — main), внутренняя синхронизация не требуется.
 *
 * @param T Тип данных, возвращаемых источником.
 * @param config Параметры интервалов и backoff.
 * @param isFatalError Классификатор фатальных ошибок: true — прекратить опрос.
 * @param fetch Источник данных, например вызов use-case. Должен быть main-safe
 *   (Retrofit suspend это гарантирует) и не бросать исключений, кроме CancellationException.
 */
class SmartPoller<T>(
    private val config: SmartPollerConfig = SmartPollerConfig(),
    private val isFatalError: (AppException) -> Boolean = { it is SessionExpiredDomainException },
    private val fetch: suspend () -> CustomResult<T>
) {

    /** Задача активного опроса; null или неактивна, когда опрос остановлен. */
    private var pollingJob: Job? = null

    /** Признак активного опроса. */
    val isRunning: Boolean
        get() = pollingJob?.isActive == true

    /**
     * Холодный поток результатов опроса — основная форма API.
     *
     * Подписка запускает цикл, отмена подписки останавливает его и отменяет
     * текущий запрос (через кооперативную отмену корутин). Это делает polling
     * взаимозаменяемым с SSE/WebSocket-потоком из observe-use-case:
     * ViewModel собирает их одинаковым collect.
     */
    fun asFlow(): Flow<CustomResult<T>> = flow {
        var delayMs = config.baseIntervalMs
        while (currentCoroutineContext().isActive) {
            val result = fetch()
            when (result) {
                is CustomResult.Success -> {
                    delayMs = config.baseIntervalMs
                    emit(result)
                }
                is CustomResult.Error -> {
                    emit(result)
                    if (isFatalError(result.exception)) return@flow
                }
            }
            delay(withJitter(delayMs))
            // Backoff наращивается после ожидания: первая неудача ждёт базовую паузу,
            // каждая следующая — вдвое больше, до верхнего предела.
            if (result is CustomResult.Error) {
                delayMs = (delayMs * config.backoffMultiplier).toLong()
                    .coerceAtMost(config.maxIntervalMs)
            }
        }
    }.distinctUntilChanged()

    /**
     * Запускает опрос в переданной области корутин и отдаёт каждый результат в [onResult].
     *
     * Идемпотентен: повторный вызов при активном опросе игнорируется,
     * поэтому безопасно вызывать на каждый ON_START экрана.
     *
     * @param scope Область, задающая время жизни опроса (например, viewModelScope).
     * @param onResult Обработчик очередного результата (вызывается последовательно).
     */
    fun start(scope: CoroutineScope, onResult: (CustomResult<T>) -> Unit) {
        if (isRunning) return
        pollingJob = scope.launch {
            asFlow().collect { onResult(it) }
        }
    }

    /**
     * Останавливает опрос: отменяет и таймер, и выполняющийся запрос.
     * Идемпотентен — безопасно вызывать на каждый ON_STOP экрана и при logout.
     */
    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Добавляет к паузе случайный разброс согласно [SmartPollerConfig.jitterRatio].
     *
     * @param baseMs Номинальная пауза, мс.
     * @return Пауза со случайным отклонением, не менее 1 мс.
     */
    private fun withJitter(baseMs: Long): Long {
        if (config.jitterRatio == 0.0) return baseMs
        val spread = baseMs * config.jitterRatio
        return (baseMs - spread + Random.nextDouble() * 2 * spread)
            .toLong().coerceAtLeast(1L)
    }
}