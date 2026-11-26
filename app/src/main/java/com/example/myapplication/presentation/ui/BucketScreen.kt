package com.example.myapplication.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Bucket
import com.example.domain.model.BucketStatus
import com.example.myapplication.presentation.util.AppElevation
import com.example.myapplication.presentation.util.DateTimeFormatters
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.viewmodel.BucketUiState
import com.example.myapplication.presentation.viewmodel.BucketViewModel
import com.example.myapplication.presentation.viewmodel.LoadCardUiState
import kotlinx.coroutines.launch

/**
 * Варианты фильтрации по заполненности телеги.
 */
enum class FullnessFilter(val title: String) {
    ALL("Любая"),
    FULL("Загружена"),
    EMPTY("Пуста")
}

/**
 * Состояние фильтров экрана телег.
 *
 * @property selectedStatuses Набор статусов, которые должны отображаться.
 * @property fullnessFilter Фильтр по заполненности.
 * @property hideUnderRepair Флаг принудительного скрытия телег на ремонте
 *                           (работает даже если статус выбран в списке).
 */
data class BucketFilterState(
    val selectedStatuses: Set<BucketStatus> = BucketStatus.entries.toSet(),
    val fullnessFilter: FullnessFilter = FullnessFilter.ALL,
    val hideUnderRepair: Boolean = false
) {
    /**
     * Применяет фильтры к списку телег.
     * Null-статусы трактуются как [BucketStatus.UNKNOWN] в соответствии с UI-маппингом.
     */
    fun apply(buckets: List<Bucket>): List<Bucket> = buckets.filter { bucket ->
        val effectiveStatus = bucket.status ?: BucketStatus.UNKNOWN
        val statusMatch = effectiveStatus in selectedStatuses
        val fullnessMatch = when (fullnessFilter) {
            FullnessFilter.ALL -> true
            FullnessFilter.FULL -> bucket.isFull
            FullnessFilter.EMPTY -> !bucket.isFull
        }
        val repairMatch = !(hideUnderRepair && effectiveStatus == BucketStatus.UNDER_REPAIR)
        statusMatch && fullnessMatch && repairMatch
    }
}

/**
 * Saver для сохранения состояния фильтров при смене конфигурации и смерти процесса.
 * Сериализует enum-значения как строки, чтобы не зависеть от порядка ordinal.
 */
private val BucketFilterStateSaver: Saver<BucketFilterState, Any> = Saver(
    save = { state ->
        listOf(
            state.selectedStatuses.map { it.name },
            state.fullnessFilter.name,
            state.hideUnderRepair
        )
    },
    restore = { saved ->
        @Suppress("UNCHECKED_CAST")
        val list = saved as List<Any>
        val statuses = (list[0] as List<String>).mapNotNull { name ->
            BucketStatus.entries.find { it.name == name }
        }.toSet()
        val fullness = FullnessFilter.entries.find { it.name == list[1] as String } ?: FullnessFilter.ALL
        val hideRepair = list[2] as Boolean
        BucketFilterState(
            selectedStatuses = statuses.ifEmpty { BucketStatus.entries.toSet() },
            fullnessFilter = fullness,
            hideUnderRepair = hideRepair
        )
    }
)

/**
 * Экран мониторинга бетоновозных телег.
 * Отображает список телег с возможностью раскрытия детальной информации по каждой.
 *
 * В шапке экрана размещены кнопки «Фильтры» и «Обозначения статусов»:
 * фильтры применяются локально к уже загруженному списку, поэтому не влияют
 * на сетевой опрос и сохраняют позицию скролла и раскрытые карточки.
 *
 * @param viewModel ViewModel для управления состоянием экрана.
 */
@Composable
fun BucketScreen(
    viewModel: BucketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadCardState by viewModel.loadCardState.collectAsStateWithLifecycle()

    // Видимость карточек-диалогов и состояние фильтров переживают смену конфигурации.
    var showStatusLegend by rememberSaveable { mutableStateOf(false) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var filterState by rememberSaveable(stateSaver = BucketFilterStateSaver) {
        mutableStateOf(BucketFilterState())
    }

    LifecycleStartEffect(Unit) {
        viewModel.startMonitoring()
        onStopOrDispose { viewModel.stopMonitoring() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Кнопки управления представлением списка.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Md, vertical = Spacing.Xs),
            horizontalArrangement = Arrangement.End
        ) {
            BucketFilterButton(
                onClick = { showFilterDialog = true },
                isActive = filterState != BucketFilterState()
            )
            StatusLegendButton(onClick = { showStatusLegend = true })
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val state = uiState) {
                is BucketUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BucketUiState.Success -> {
                    val filteredBuckets = filterState.apply(state.bucketList)
                    if (filteredBuckets.isEmpty()) {
                        Text(
                            text = if (state.bucketList.isEmpty()) {
                                "Бетоновозные телеги не найдены"
                            } else {
                                "Телеги не найдены по заданным фильтрам"
                            },
                            modifier = Modifier.align(Alignment.Center).padding(Spacing.Md),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        BucketList(
                            buckets = filteredBuckets,
                            onLoadClick = viewModel::openLoadCard
                        )
                    }
                }
                is BucketUiState.Error -> {
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
                        Button(onClick = { viewModel.loadBucketList() }) { Text("Повторить") }
                    }
                }
            }
        }
    }

    if (loadCardState !is LoadCardUiState.Hidden) {
        LoadCardDialog(
            state = loadCardState,
            onRetry = viewModel::openLoadCard,
            onDismiss = viewModel::closeLoadCard
        )
    }

    if (showStatusLegend) {
        StatusLegendDialog(
            title = "Обозначения статусов телег",
            sections = bucketStatusLegendSections(),
            onDismiss = { showStatusLegend = false }
        )
    }

    if (showFilterDialog) {
        BucketFilterDialog(
            state = filterState,
            onStateChange = { filterState = it },
            onReset = { filterState = BucketFilterState() },
            onDismiss = { showFilterDialog = false }
        )
    }
}

/**
 * Кнопка открытия карточки фильтров.
 *
 * При наличии активных фильтров (отличных от дефолтных) иконка и текст
 * окрашиваются в primary-цвет, чтобы пользователь видел, что список отфильтрован.
 *
 * @param onClick Колбэк открытия диалога фильтров.
 * @param isActive true, если хотя бы один параметр фильтра отличается от дефолтного.
 * @param modifier Модификатор кнопки.
 */
@Composable
private fun BucketFilterButton(
    onClick: () -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    TextButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.FilterAlt,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(IconSize.Inline)
        )
        Spacer(modifier = Modifier.width(Spacing.Xsm))
        Text(text = "Фильтр", color = tint)
    }
}

/**
 * Карточка-диалог настройки фильтров списка телег.
 *
 * Содержит три секции:
 * 1. Заполненность — мультивыбор через FilterChip (используется как single-select).
 * 2. Скрытие телег на ремонте — переключатель Switch.
 * 3. Статусы — мультивыбор FilterChip с иконками и цветами из легенды.
 *
 * Сброс возвращает состояние к дефолтному (все статусы включены, заполненность любая,
 * ремонт не скрывается). Изменения применяются мгновенно, кнопка «Применить» просто
 * закрывает диалог.
 *
 * @param state Текущее состояние фильтров.
 * @param onStateChange Колбэк обновления состояния.
 * @param onReset Колбэк сброса фильтров в дефолтное состояние.
 * @param onDismiss Колбэк закрытия диалога.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BucketFilterDialog(
    state: BucketFilterState,
    onStateChange: (BucketFilterState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры списка телег") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.Md)
            ) {
                // 1. Заполненность
                Text(
                    text = "Заполненность",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Xs)
                ) {
                    FullnessFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = state.fullnessFilter == filter,
                            onClick = { onStateChange(state.copy(fullnessFilter = filter)) },
                            label = { Text(filter.title) }
                        )
                    }
                }

                HorizontalDivider()

                // 2. Скрытие ремонта
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Скрывать телеги на ремонте",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.hideUnderRepair,
                        onCheckedChange = { onStateChange(state.copy(hideUnderRepair = it)) }
                    )
                }

                HorizontalDivider()

                // 3. Статусы
                Text(
                    text = "Статусы",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Xs)
                ) {
                    BucketStatus.entries.forEach { status ->
                        val item = bucketStatusLegendItem(status)
                        FilterChip(
                            selected = status in state.selectedStatuses,
                            onClick = {
                                val newStatuses = if (status in state.selectedStatuses) {
                                    state.selectedStatuses - status
                                } else {
                                    state.selectedStatuses + status
                                }
                                onStateChange(state.copy(selectedStatuses = newStatuses))
                            },
                            label = { Text(item.title) },
                            leadingIcon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.tint,
                                    modifier = Modifier.size(IconSize.Sm)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Применить") }
        },
        dismissButton = {
            TextButton(onClick = onReset) { Text("Сбросить") }
        }
    )
}

/**
 * Список бетоновозных телег с возможностью раскрытия деталей и автоматической прокруткой.
 *
 * @param buckets Список моделей телег.
 * @param onLoadClick Колбэк клика по номеру заявки (открытие карточки заявки).
 */
@Composable
private fun BucketList(
    buckets: List<Bucket>,
    onLoadClick: (Long) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.Sm)
    ) {
        itemsIndexed(buckets, key = { _, bucket -> bucket.id }) { index, bucket ->
            BucketItem(
                bucket = bucket,
                onLoadClick = onLoadClick,
                onExpandRequest = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(index)
                    }
                }
            )
        }
    }
}

/**
 * Элемент списка бетоновозной телеги.
 * В закрытом состоянии отображает имя, статус и флаг загруженности.
 * В раскрытом состоянии отображает детальную информацию.
 *
 * @param bucket Модель бетоновозной телеги.
 * @param onLoadClick Колбэк клика по номеру заявки (открытие карточки заявки).
 * @param onExpandRequest Колбэк, вызываемый при раскрытии элемента для инициирования прокрутки.
 */
@Composable
private fun BucketItem(
    bucket: Bucket,
    onLoadClick: (Long) -> Unit,
    onExpandRequest: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.Card)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val willExpand = !isExpanded
                        isExpanded = willExpand
                        if (willExpand) {
                            onExpandRequest()
                        }
                    }
                    .padding(Spacing.Md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bucket.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.Md))
                IsFullIcon(isFull = bucket.isFull)
                Spacer(modifier = Modifier.width(Spacing.Md))
                BucketStatusIcon(status = bucket.status)
                Spacer(modifier = Modifier.width(Spacing.Sm))
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    modifier = Modifier
                        .size(IconSize.Md)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                HorizontalDivider()
                ExpandedBucketContent(
                    bucket = bucket,
                    onLoadClick = onLoadClick
                )
            }
        }
    }
}

/**
 * Иконка, отображающая флаг загруженности телеги.
 *
 * Визуальные параметры берутся из [bucketFullnessLegendItem] — единого
 * источника для иконок списка и карточки-легенды.
 *
 * @param isFull true, если телега загружена, false - если пуста.
 */
@Composable
private fun IsFullIcon(isFull: Boolean) {
    val item = bucketFullnessLegendItem(isFull = isFull)

    Icon(
        imageVector = item.icon,
        contentDescription = item.title,
        tint = item.tint,
        modifier = Modifier.size(IconSize.Md)
    )
}

/**
 * Детальная информация о телеге в раскрытом состоянии.
 *
 * @param bucket Модель бетоновозной телеги.
 * @param onLoadClick Колбэк клика по номеру активной заявки (открытие карточки заявки).
 */
@Composable
private fun ExpandedBucketContent(
    bucket: Bucket,
    onLoadClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Md),
        verticalArrangement = Arrangement.spacedBy(Spacing.Smd)
    ) {
        Text(
            text = "Местоположение",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        InfoRow(label = "Текущий участок", value = bucket.zoneNumber.toString())
        InfoRow(label = "Участок назначения", value = bucket.dstZone.toString())

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Xs))

        Text(
            text = "Информация о заявке",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        if (bucket.loadId != 0L) {
            LoadIdRow(
                loadId = bucket.loadId,
                onClick = { onLoadClick(bucket.loadId) }
            )
        } else {
            InfoRow(label = "Код заявки", value = "Нет активной заявки")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Xs))

        Text(
            text = "Временные отметки",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        InfoRow(
            label = "Приготовление смеси",
            value = bucket.loadDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
        InfoRow(
            label = "Прибытие в точку разгрузки",
            value = bucket.arrivalDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
        InfoRow(
            label = "Завершение разгрузки",
            value = bucket.departDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
    }
}

/**
 * Строка с кликабельным номером активной заявки.
 *
 * @param loadId Код активной заявки на отгрузку.
 * @param onClick Колбэк клика по номеру заявки.
 */
@Composable
private fun LoadIdRow(
    loadId: Long,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Код заявки",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Spacing.Sm))
        Text(
            text = loadId.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .clickable(onClickLabel = "Открыть карточку заявки", onClick = onClick)
                .padding(vertical = Spacing.Xs)
        )
    }
}

/**
 * Строка для отображения пары "метка-значение" в раскрытом списке.
 *
 * @param label Название поля.
 * @param value Значение поля.
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Spacing.Sm))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Визуальные обозначения статусов бетоновозной телеги.
 *
 * @param status Статус телеги ([BucketStatus]).
 * @param modifier Модификатор.
 */
@Composable
fun BucketStatusIcon(
    status: BucketStatus?,
    modifier: Modifier = Modifier
) {
    val item = bucketStatusLegendItem(status = status)

    Icon(
        imageVector = item.icon,
        contentDescription = item.title,
        tint = item.tint,
        modifier = modifier.size(IconSize.Md)
    )
}