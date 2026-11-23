package com.example.myapplication.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.domain.event.AuthEventBus
import com.example.domain.model.Bucket
import com.example.domain.model.BucketStatus
import com.example.domain.model.DstPoint
import com.example.domain.usecase.GetBucketListUseCase
import com.example.domain.usecase.GetDstPointListUseCase
import com.example.domain.util.CustomResult
import com.example.myapplication.presentation.ui.DstPointListScreen
import com.example.myapplication.presentation.viewmodel.DstPointListViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import kotlin.math.abs

/**
 * UI-тесты экрана точек выгрузки: кнопка «Выгрузить», Snackbar о прибытии
 * и временная зелёная подсветка строки.
 *
 * Анимация проверяется попиксельно: фон строки захватывается через
 * captureToImage в момент завершения fade-in и после fade-out. Время
 * проматывается вручную (mainClock.autoAdvance = false), реальный поллер
 * «заморожен» (fetch висит в awaitCancellation), события подаются через
 * debugInjectBuckets — тест полностью детерминирован и не зависит от сети.
 */
class DstPointListScreenTest {

    @get:Rule
    val rule = createComposeRule()

    /** Полный цикл: прибытие → кнопка + Snackbar + зелёный фон → затухание → отъезд → кнопки нет. */
    @Test
    fun arrival_showsButton_snackbar_andTemporaryGreenHighlight() {
        rule.mainClock.autoAdvance = false
        val vm = buildViewModel()
        rule.setContent { DstPointListScreen(onProductList = {}, viewModel = vm) }
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()
        rule.onNodeWithText("Цех №1").assertExists()

        vm.debugInjectBuckets(emptyList())                 // базовый слепок
        vm.debugInjectBuckets(listOf(bucket(7, zone = 1))) // прибытие
        rule.mainClock.advanceTimeBy(1_000)                // коллектор события + старт эффектов
        rule.waitForIdle()

        rule.onNodeWithTag("unload-button-1").assertExists()
        rule.onNodeWithText("прибыла на точку", substring = true).assertExists()

        rule.mainClock.advanceTimeBy(700)                  // fade-in (600 мс) завершён
        val during = rowBackgroundPixel()
        assertTrue(
            "Фон строки должен быть зеленоватым во время подсветки: $during",
            during.green > during.red + 0.05f
        )

        rule.mainClock.advanceTimeBy(5_000 + 700)          // удержание 5с + fade-out 600мс
        rule.waitForIdle()
        val after = rowBackgroundPixel()
        assertTrue(
            "После 5с подсветка должна затухнуть до фона: $after",
            abs(after.green - after.red) < 0.03f
        )

        vm.debugInjectBuckets(emptyList())                 // телега уехала
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithTag("unload-button-1").assertDoesNotExist()
    }

    /** Повторное прибытие на ту же точку перезапускает подсветку (механика счётчика тиков). */
    @Test
    fun repeatArrival_restartsHighlight() {
        rule.mainClock.autoAdvance = false
        val vm = buildViewModel()
        rule.setContent { DstPointListScreen(onProductList = {}, viewModel = vm) }
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()

        vm.debugInjectBuckets(emptyList())
        vm.debugInjectBuckets(listOf(bucket(7, zone = 1)))
        rule.mainClock.advanceTimeBy(5_000 + 1_500)        // первая подсветка полностью погасла
        rule.waitForIdle()
        val faded = rowBackgroundPixel()
        assertTrue(abs(faded.green - faded.red) < 0.03f)

        vm.debugInjectBuckets(listOf(bucket(7, zone = 1), bucket(9, zone = 1))) // вторая телега
        rule.mainClock.advanceTimeBy(1_500)
        rule.waitForIdle()
        val relit = rowBackgroundPixel()
        assertTrue("Повторное прибытие должно снова зажечь строку: $relit", relit.green > relit.red + 0.05f)
    }

    /** Пиксель фона строки зоны 1: у левого края (внутри padding, левее текста). */
    private fun rowBackgroundPixel(): Color {
        val pixels = rule.onNodeWithTag("dst-row-1").captureToImage().toPixelMap()
        return pixels[2, pixels.height / 2]
    }

    /**
     * ViewModel с «замороженным» поллером: fetch никогда не completes,
     * поэтому реальные опросы не перетирают состояние, подаваемое тестом.
     */
    private fun buildViewModel(): DstPointListViewModel {
        val dst: GetDstPointListUseCase = mockk()
        val buckets: GetBucketListUseCase = mockk()
        val auth: AuthEventBus = mockk()
        coEvery { dst() } returns CustomResult.Success(
            listOf(
                DstPoint(zoneId = 1, zoneName = "Цех №1", departmentId = "1", postId = "1"),
                DstPoint(zoneId = 2, zoneName = "Цех №2", departmentId = "2", postId = "2")
            )
        )
        coEvery { buckets() } coAnswers { awaitCancellation() }
        every { auth.logoutEvents } returns MutableSharedFlow()
        return DstPointListViewModel(dst, buckets, auth)
    }

    /** Фабрика телеги, стоящей на своей точке выгрузки (зона назначения == текущая). */
    private fun bucket(id: Int, zone: Int): Bucket = Bucket(
        id = id,
        name = "Бетоновоз $id",
        zoneNumber = zone,
        isFull = true,
        status = BucketStatus.MOVING,
        loadId = 9_000L + id,
        dstZone = zone,
        loadDate = LocalDateTime.of(2026, 11, 19, 12, 0),
        arrivalDate = LocalDateTime.of(2026, 11, 19, 12, 0),
        departDate = null
    )
}