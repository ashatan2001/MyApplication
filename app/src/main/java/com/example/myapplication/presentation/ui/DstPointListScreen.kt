package com.example.myapplication.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Bucket
import com.example.domain.model.DstPoint
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.util.StatusColors
import com.example.myapplication.presentation.viewmodel.DstPointListEvent
import com.example.myapplication.presentation.viewmodel.DstPointListUiState
import com.example.myapplication.presentation.viewmodel.DstPointListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Длительность зелёной подсветки строки после прибытия телеги, мс. */
private const val ARRIVAL_HIGHLIGHT_DURATION_MS = 5_000L

/** Длительность анимации появления/затухания подсветки строки, мс. */
private const val ARRIVAL_HIGHLIGHT_ANIMATION_MS = 600

/** Прозрачность зелёной подсветки строки: фон не мешает читать текст. */
private const val ARRIVAL_HIGHLIGHT_ALPHA = 0.2f

/**
 * Экран списка точек выгрузки бетона.
 * Отображает список доступных зон в виде легкой таблицы без карточек.
 * Навигация "Назад" обрабатывается глобальной панелью [MainShell].
 *
 * Мониторинг прибытия телег (пока экран в состоянии STARTED):
 * - телега на точке -> в строке появляется кнопка «Выгрузить» (нет телеги — нет кнопки);
 * - новое прибытие -> Snackbar-уведомление и временная зелёная подсветка строки.
 *
 * @param onProductList Колбэк, вызываемый при выборе точки выгрузки, передает ID зоны.
 * @param viewModel [DstPointListViewModel] для управления загрузкой списка точек
 *   и мониторингом прибытия телег.
 */
@Composable
fun DstPointListScreen(
    onProductList: (Int) -> Unit,
    viewModel: DstPointListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val arrivedBuckets by viewModel.arrivedBuckets.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * Счётчики событий прибытия по зонам: zoneId -> номер последнего события.
     * Инкремент счётчика перезапускает таймер подсветки в строке списка,
     * поэтому повторное прибытие на ту же точку снова включает подсветку.
     */
    val arrivalHighlightTicks = remember { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(Unit) {
        viewModel.loadDstPointList()
    }

    /**
     * Мониторинг телег активен только пока экран в состоянии STARTED:
     * уход на другой экран или сворачивание приложения останавливает опрос,
     * возврат — возобновляет (первый запрос уходит сразу при старте).
     */
    LifecycleStartEffect(Unit) {
        viewModel.startBucketMonitoring()
        onStopOrDispose { viewModel.stopBucketMonitoring() }
    }

    // Одноразовые события: уведомление о прибытии телеги + подсветка строки.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DstPointListEvent.BucketArrived -> {
                    // Подсветка стартует немедленно, не дожидаясь очереди Snackbar.
                    arrivalHighlightTicks[event.zoneId] =
                        (arrivalHighlightTicks[event.zoneId] ?: 0) + 1
                    // SnackbarHostState сам сериализует показ нескольких сообщений.
                    launch {
                        snackbarHostState.showSnackbar(
                            message = "Телега «${event.bucketName}» прибыла " +
                                    "на точку «${event.zoneName}»",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        // Insets уже обработаны оболочкой MainShell — не дублируем отступы.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is DstPointListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DstPointListUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Поле поиска
                        SearchField(
                            text = "Поиск подразделения",
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.Md, vertical = Spacing.Sm)
                        )

                        // Фильтрация списка
                        val filteredList = state.dstPointList.filter { point ->
                            point.zoneName.contains(searchQuery.trim(), ignoreCase = true)
                        }

                        if (filteredList.isEmpty()) {
                            // Состояние пустого списка или отсутствия результатов поиска
                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "Ничего не найдено по запросу \"$searchQuery\""
                                else
                                    "Список точек выгрузки бетона пуст",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.Lg),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {

                                // Легкий заголовок секции
                                item(key = "header") {
                                    Text(
                                        text = "Подразделения",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = Spacing.Md, vertical = Spacing.Smd)
                                    )
                                    HorizontalDivider()
                                }

                                items(items = filteredList, key = { point -> point.zoneId }) { point ->
                                    DstPointRow(
                                        dstPoint = point,
                                        arrivedBucket = arrivedBuckets[point.zoneId],
                                        arrivalTick = arrivalHighlightTicks[point.zoneId] ?: 0,
                                        onOrder = { onProductList(point.zoneId) },
                                        onUnload = { bucket ->
                                            /* Логика выгрузки телеги bucket (заявка bucket.loadId) */
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Md))
                                }
                            }
                        }
                    }
                }

                is DstPointListUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(Spacing.Lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Md)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(IconSize.Xl)
                        )
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.loadDstPointList() }) { Text("Повторить") }
                    }
                }
            }

            /**
             * Отладочная панель симуляции телег: в debug-сборке рисуется поверх низа
             * списка, в release — no-op (пустая реализация из release source set).
             */
            DstPointDebugPanel(
                viewModel = viewModel,
                zones = (uiState as? DstPointListUiState.Success)?.dstPointList.orEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Отдельная строка списка точек выгрузки.
 * Выполнена в минималистичном стиле без использования Surface/Card для снижения нагрузки на UI.
 *
 * Кнопка «Выгрузить» отображается только при наличии телеги на точке ([arrivedBucket] != null).
 * При новом прибытии ([arrivalTick] увеличивается) строка плавно подсвечивается зелёным
 * на [ARRIVAL_HIGHLIGHT_DURATION_MS] мс, затем фон плавно возвращается к прозрачному.
 *
 * @param dstPoint Модель данных точки выгрузки.
 * @param arrivedBucket Телега, находящаяся на точке; null — телеги нет, кнопка выгрузки скрыта.
 * @param arrivalTick Счётчик событий прибытия на эту точку; рост значения запускает подсветку.
 * @param onUnload Действие при нажатии на кнопку выгрузки; получает прибывшую телегу.
 * @param onOrder Действие при нажатии на кнопку заказа.
 */
@Composable
private fun DstPointRow(
    dstPoint: DstPoint,
    arrivedBucket: Bucket?,
    arrivalTick: Int,
    onUnload: (Bucket) -> Unit,
    onOrder: () -> Unit
) {
    // Временная подсветка: каждое новое событие прибытия включает зелёный фон на 5 секунд.
    var isHighlighted by remember { mutableStateOf(false) }
    LaunchedEffect(arrivalTick) {
        if (arrivalTick > 0) {
            isHighlighted = true
            delay(ARRIVAL_HIGHLIGHT_DURATION_MS)
            isHighlighted = false
        }
    }
    val rowBackground by animateColorAsState(
        targetValue = if (isHighlighted) {
            StatusColors.Success.copy(alpha = ARRIVAL_HIGHLIGHT_ALPHA)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = ARRIVAL_HIGHLIGHT_ANIMATION_MS),
        label = "arrivalRowHighlight"
    )

    /**
     * Модификатор строки: testTag позволяет UI-тесту захватить именно эту строку
     * и проверить цвет фона в моменты появления/затухания подсветки.
     */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dst-row-${dstPoint.zoneId}")
            .background(rowBackground)
            .padding(horizontal = Spacing.Md, vertical = Spacing.Smd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Наименование подразделения
        Text(
            text = dstPoint.zoneId.toString() + " — " + dstPoint.zoneName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(Spacing.Sm))

        // Кнопка выгрузки: появляется только при прибытии телеги на точку
        if (arrivedBucket != null) {
            TextButton(
                onClick = { onUnload(arrivedBucket) },
                contentPadding = PaddingValues(horizontal = Spacing.Sm),
                modifier = Modifier.testTag("unload-button-${dstPoint.zoneId}")
            ) {
                Icon(
                    imageVector = Icons.Default.Downloading,
                    contentDescription = "Выгрузить",
                    modifier = Modifier.size(IconSize.Inline)
                )
                /*Spacer(modifier = Modifier.width(Spacing.Xs))
                Text(
                    text = "Выгрузить",
                    style = MaterialTheme.typography.labelMedium,
                    color = StatusColors.Success
                )*/
            }
        }

        // Кнопка заказа
        TextButton(
            onClick = onOrder,
            contentPadding = PaddingValues(horizontal = Spacing.Sm),
            modifier = Modifier.testTag("order-button-${dstPoint.zoneId}")
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = "Заказать",
                modifier = Modifier.size(IconSize.Inline),
                tint = MaterialTheme.colorScheme.secondary
            )
            /*Spacer(modifier = Modifier.width(Spacing.Xs))
            Text(
                text = "Заказать",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )*/
        }
    }
}