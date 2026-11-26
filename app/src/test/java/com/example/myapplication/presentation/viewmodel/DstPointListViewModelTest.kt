package com.example.myapplication.presentation.viewmodel

import com.example.domain.event.AuthEventBus
import com.example.domain.exception.NetworkConnectionException
import com.example.domain.exception.SessionExpiredDomainException
import com.example.domain.model.Bucket
import com.example.domain.model.BucketStatus
import com.example.domain.model.DstPoint
import com.example.domain.usecase.GetBucketListUseCase
import com.example.domain.usecase.GetDstPointListUseCase
import com.example.domain.util.CustomResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit-тесты мониторинга прибытия телег в [DstPointListViewModel].
 *
 * Поллер работает на виртуальном времени (StandardTestDispatcher): каждый
 * шаг сценария проматывает 60 виртуальных секунд — заведомо больше любого
 * интервала опроса с jitter/backoff. Ответ между шагами фиксирован, поэтому
 * лишние циклы безвредны: дедупликация поллера и diff по id гасят повторы.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DstPointListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val points = listOf(point(1, "Цех №1"), point(2, "Цех №2"))

    private lateinit var dstUseCase: GetDstPointListUseCase
    private lateinit var bucketUseCase: GetBucketListUseCase
    private lateinit var authBus: AuthEventBus

    /** Текущий «ответ сервера», меняется между шагами сценария. */
    private var bucketResponse: CustomResult<List<Bucket>> = CustomResult.Success(emptyList())

    /** Счётчик обращений поллера к use-case (проверка остановки опроса). */
    private var fetchCount = 0

    private var vm: DstPointListViewModel? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        dstUseCase = mockk()
        bucketUseCase = mockk()
        authBus = mockk()
        coEvery { dstUseCase() } returns CustomResult.Success(points)
        coEvery { bucketUseCase() } coAnswers { fetchCount++; bucketResponse }
        every { authBus.logoutEvents } returns MutableSharedFlow()
    }

    @After
    fun tearDown() {
        // Останавливаем поллер даже при упавшем ассерте, чтобы виртуальное
        // время не осталось с бесконечными delay-задачами.
        vm?.stopBucketMonitoring()
        Dispatchers.resetMain()
    }

    /** Первый опрос — базовый слепок: кнопка есть, уведомлений нет. */
    @Test
    fun firstPoll_showsButton_withoutArrivalEvent() = runTest {
        bucketResponse = CustomResult.Success(listOf(bucket(7, zone = 1)))
        val model = createViewModel()
        val events = collectEvents(model)

        model.startBucketMonitoring()
        advanceTimeBy(60_000L); runCurrent()

        assertTrue(model.arrivedBuckets.value.containsKey(1))
        assertTrue("Базовый опрос не должен порождать событий", events.isEmpty())
    }

    /** Появление телеги на точке: ровно одно событие + запись в карте; повторы ответа молчат. */
    @Test
    fun arrival_emitsSingleEvent_andRepeatedPollsStaySilent() = runTest {
        val model = createViewModel()
        val events = collectEvents(model)
        model.loadDstPointList(); runCurrent()
        model.startBucketMonitoring()
        advanceTimeBy(60_000L); runCurrent() // baseline: пусто

        bucketResponse = CustomResult.Success(listOf(bucket(7, zone = 1)))
        advanceTimeBy(60_000L); runCurrent()

        assertEquals(1, events.size)
        val event = events.first() as DstPointListEvent.BucketArrived
        assertEquals(1, event.zoneId)
        assertEquals("Бетоновоз 7", event.bucketName)
        assertEquals("Цех №1", event.zoneName)
        assertEquals(7, model.arrivedBuckets.value[1]?.id)

        advanceTimeBy(60_000L); runCurrent() // тот же ответ ещё несколько циклов
        assertEquals("Повторный идентичный ответ не должен давать событие", 1, events.size)
    }

    /** Отъезд телеги: кнопка исчезает, событие НЕ шлётся (это не прибытие). */
    @Test
    fun departure_hidesButton_withoutEvent() = runTest {
        bucketResponse = CustomResult.Success(listOf(bucket(7, zone = 1)))
        val model = createViewModel()
        val events = collectEvents(model)
        model.startBucketMonitoring()
        advanceTimeBy(60_000L); runCurrent()

        bucketResponse = CustomResult.Success(emptyList())
        advanceTimeBy(60_000L); runCurrent()

        assertFalse(model.arrivedBuckets.value.containsKey(1))
        assertTrue(events.isEmpty())
    }

    /** Завершённая разгрузка (departDate): телега ещё в зоне, но кнопка скрыта. */
    @Test
    fun finishedUnloading_hidesButton() = runTest {
        bucketResponse = CustomResult.Success(listOf(bucket(7, zone = 1)))
        val model = createViewModel()
        model.startBucketMonitoring()
        advanceTimeBy(60_000L); runCurrent()

        bucketResponse = CustomResult.Success(
            listOf(bucket(7, zone = 1, departDate = LocalDateTime.of(2026, 11, 19, 12, 30)))
        )
        advanceTimeBy(60_000L); runCurrent()

        assertFalse(model.arrivedBuckets.value.containsKey(1))
    }

    /** Вторая телега на той же точке: новое событие, кнопка остаётся у раньше прибывшей. */
    @Test
    fun secondBucketAtSameZone_emitsEvent_andKeepsEarliestForButton() = runTest {
        val early = LocalDateTime.of(2026, 11, 19, 12, 0)
        bucketResponse = CustomResult.Success(listOf(bucket(7, zone = 1, arrivalDate = early)))
        val model = createViewModel()
        val events = collectEvents(model)
        model.startBucketMonitoring()
        advanceTimeBy(60_000L); runCurrent()

        bucketResponse = CustomResult.Success(
            listOf(
                bucket(7, zone = 1, arrivalDate = early),
                bucket(9, zone = 1, arrivalDate = early.plusMinutes(1))
            )
        )
        advanceTimeBy(60_000L); runCurrent()

        assertEquals(1, events.size)
        assertEquals("Бетоновоз 9", (events.first() as DstPointListEvent.BucketArrived).bucketName)
        assertEquals("Для кнопки выбирается раньше прибывшая", 7, model.arrivedBuckets.value[1]?.id)
    }

    /** Ошибка сети не затирает уже показанную кнопку. */
    @Test
    fun networkError_keepsLastKnownState() = runTest {
        bucketResponse = CustomResult.Success(listOf(bucket(7, zone = 1)))
        val model = createViewModel()
        model.startBucketMonitoring()
        advanceTimeBy(60_000L); runCurrent()

        bucketResponse = CustomResult.Error(mockk<NetworkConnectionException>(relaxed = true))
        advanceTimeBy(60_000L); runCurrent()

        assertEquals(7, model.arrivedBuckets.value[1]?.id)
    }

    /** Истёкшая сессия — фатальная ошибка: опрос прекращается навсегда. */
    @Test
    fun sessionExpired_stopsPolling() = runTest {
        val model = createViewModel()
        model.startBucketMonitoring()
        advanceTimeBy(60_000L); runCurrent()

        bucketResponse = CustomResult.Error(mockk<SessionExpiredDomainException>(relaxed = true))
        advanceTimeBy(120_000L); runCurrent()
        val afterFatal = fetchCount

        advanceTimeBy(120_000L); runCurrent()
        assertEquals("После фатальной ошибки запросы прекращаются", afterFatal, fetchCount)
    }

    /** Создаёт ViewModel с подменёнными зависимостями и запоминает её для tearDown. */
    private fun createViewModel(): DstPointListViewModel =
        DstPointListViewModel(dstUseCase, bucketUseCase, authBus).also { vm = it }

    /** Собирает одноразовые события в список на фоне тестового планировщика. */
    private fun TestScope.collectEvents(model: DstPointListViewModel): MutableList<DstPointListEvent> {
        val events = mutableListOf<DstPointListEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.events.toList(events) }
        return events
    }

    /** Фабрика точки выгрузки (единое место подгонки под конструктор модели). */
    private fun point(id: Int, name: String): DstPoint =
        DstPoint(zoneId = id, zoneName = name, departmentId = id.toString(), postId = id.toString())

    /**
     * Фабрика телеги «на своей точке»: zoneNumber == dstZone, активная заявка.
     *
     * @param id Код телеги.
     * @param zone Зона, в которой телега стоит и куда назначена.
     * @param status Статус телеги.
     * @param arrivalDate Отметка прибытия (для порядка выбора на точку).
     * @param departDate Отметка завершения разгрузки; null — разгрузка не завершена.
     */
    private fun bucket(
        id: Int,
        zone: Int,
        status: BucketStatus = BucketStatus.MOVING,
        arrivalDate: LocalDateTime? = LocalDateTime.of(2026, 11, 19, 12, 0),
        departDate: LocalDateTime? = null
    ): Bucket = Bucket(
        id = id,
        name = "Бетоновоз $id",
        zoneNumber = zone,
        isFull = true,
        status = status,
        loadId = 9_000L + id,
        dstZone = zone,
        loadDate = arrivalDate,
        arrivalDate = arrivalDate,
        departDate = departDate
    )
}