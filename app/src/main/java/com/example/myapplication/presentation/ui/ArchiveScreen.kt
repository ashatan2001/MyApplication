package com.example.myapplication.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.util.*
import com.example.myapplication.presentation.util.DateTimeFormatters.parseToUtcMillis
import com.example.myapplication.presentation.viewmodel.ArchiveViewModel

/**
 * Ширина фиксированной колонки с датой в таблице.
 * Специфична для экрана архива, поэтому остаётся локальной константой файла.
 */
private val DateColumnWidth: Dp = 160.dp

/**
 * Экран архива с адаптивной версткой и скрываемой панелью фильтров:
 * [WindowSize.COMPACT] — сворачиваемая карточка фильтров над таблицей,
 * колонки таблицы с горизонтальной прокруткой;
 * [WindowSize.MEDIUM] — то же, но поля дат размещаются в две колонки для экономии высоты;
 * [WindowSize.EXPANDED] — форма в скрываемой боковой панели слева; при сворачивании
 * таблица занимает всю ширину, а над ней появляется строка-заголовок для раскрытия.
 *
 * В свёрнутом состоянии заголовок панели показывает сводку активных фильтров
 * (тип архива, период, размер страницы). Состояние раскрытия хранится в
 * rememberSaveable и переживает смену конфигурации и смерть процесса.
 *
 * @param viewModel [ArchiveViewModel] для управления состоянием экрана.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(viewModel: ArchiveViewModel = hiltViewModel()) {
    // collectAsStateWithLifecycle — остановка сбора при уходе экрана из Lifecycle.State.STARTED,
    // в отличие от collectAsState в оригинале, который продолжает собирать в фоне.
    val state by viewModel.state.collectAsStateWithLifecycle()
    val windowSize = rememberWindowSize()

    // rememberSaveable: флаги открытия пикеров переживают смерть процесса.
    var showStartPicker by rememberSaveable { mutableStateOf(false) }
    var showEndPicker by rememberSaveable { mutableStateOf(false) }

    // Видимость панели «Фильтры и сортировка»; по умолчанию раскрыта.
    var filtersExpanded by rememberSaveable { mutableStateOf(true) }

    when (windowSize) {
        // Планшет горизонтально: скрываемая форма слева, таблица справа
        WindowSize.EXPANDED -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Md),
            ) {
                // Боковая панель плавно скрывается, освобождая ширину таблице.
                // AnimatedVisibility — непосредственный ребёнок Row (RowScope),
                // поэтому перегрузка резолвится на ближайший приёмник без конфликта.
                AnimatedVisibility(
                    visible = filtersExpanded,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                    label = "filters_side_panel",
                ) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        Column(
                            modifier = Modifier
                                .width(LayoutSize.FormSidePanelWidth)
                                .fillMaxHeight(),
                        ) {
                            FiltersPanelHeader(
                                expanded = true,
                                state = state,
                                onToggle = { filtersExpanded = false },
                            )
                            HorizontalDivider()
                            ArchiveForm(
                                state = state,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = Spacing.Smd, vertical = Spacing.Sm),
                                onTypeChange = viewModel::updateType,
                                onRecordsChange = viewModel::updateRecords,
                                onLoadClick = viewModel::loadArchive,
                                onShowStartPicker = { showStartPicker = true },
                                onShowEndPicker = { showEndPicker = true },
                            )
                        }
                        VerticalDivider(modifier = Modifier.fillMaxHeight())
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Свёрнутая панель: строка-заголовок с кнопкой раскрытия над таблицей.
                    if (!filtersExpanded) {
                        FiltersPanelHeader(
                            expanded = false,
                            state = state,
                            onToggle = { filtersExpanded = true },
                        )
                    }
                    ArchiveContent(
                        state = state,
                        modifier = Modifier.weight(1f),
                        onPreviousPage = viewModel::previousPage,
                        onNextPage = viewModel::nextPage,
                        onClearError = viewModel::clearError,
                    )
                }
            }
        }

        // Телефон и планшет вертикально: сворачиваемая карточка фильтров над таблицей
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.Smd, vertical = Spacing.Sm),
            ) {
                FiltersCard(
                    expanded = filtersExpanded,
                    onToggle = { filtersExpanded = !filtersExpanded },
                    state = state,
                    onTypeChange = viewModel::updateType,
                    onRecordsChange = viewModel::updateRecords,
                    onLoadClick = viewModel::loadArchive,
                    onShowStartPicker = { showStartPicker = true },
                    onShowEndPicker = { showEndPicker = true },
                )
                Spacer(modifier = Modifier.height(Spacing.Sm))
                ArchiveContent(
                    state = state,
                    modifier = Modifier.weight(1f),
                    onPreviousPage = viewModel::previousPage,
                    onNextPage = viewModel::nextPage,
                    onClearError = viewModel::clearError,
                )
            }
        }
    }

    // Передача null (вместо System.currentTimeMillis) открывает пикер на «сегодня»,
    // а не подсвечивает случайно выбранный день при пустом поле.
    if (showStartPicker) {
        NativeDateTimePickerDialog(
            initialDateMillis = parseToUtcMillis(state.startDate),
            onDismiss = { showStartPicker = false },
            onDateTimeSelected = { formattedString ->
                viewModel.updateStartDate(formattedString)
                showStartPicker = false
            },
        )
    }
    if (showEndPicker) {
        NativeDateTimePickerDialog(
            initialDateMillis = parseToUtcMillis(state.endDate),
            onDismiss = { showEndPicker = false },
            onDateTimeSelected = { formattedString ->
                viewModel.updateEndDate(formattedString)
                showEndPicker = false
            },
        )
    }
}

/**
 * Сворачиваемая карточка «Фильтры и сортировка» для компактной вёрстки.
 *
 * Оформлена по паттерну раскрывающихся карточек проекта (BucketItem, TerminalCard):
 * кликабельная шапка с вращающимся шевроном, тело — [ArchiveForm] под разделителем.
 * Сворачивание анимировано (fade + expand/shrinkVertically); свёрнутая шапка
 * показывает сводку активных фильтров.
 *
 * AnimatedVisibility — непосредственный ребёнок ColumnScope-лямбды Card,
 * поэтому выбирается перегрузка ColumnScope.AnimatedVisibility на ближайшем
 * приёмнике (без конфликта внешних областей видимости).
 *
 * @param expanded Раскрыта ли панель фильтров.
 * @param onToggle Колбэк переключения раскрытия.
 * @param state Текущее состояние экрана (для формы и сводки фильтров).
 * @param onTypeChange Смена типа архива.
 * @param onRecordsChange Смена количества записей на странице.
 * @param onLoadClick Запуск загрузки архива.
 * @param onShowStartPicker Открытие диалога выбора даты начала.
 * @param onShowEndPicker Открытие диалога выбора даты окончания.
 */
@Composable
private fun FiltersCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    state: ArchiveScreenState,
    onTypeChange: (ArchiveType) -> Unit,
    onRecordsChange: (String) -> Unit,
    onLoadClick: () -> Unit,
    onShowStartPicker: () -> Unit,
    onShowEndPicker: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.Card),
    ) {
        FiltersPanelHeader(
            expanded = expanded,
            state = state,
            onToggle = onToggle,
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            label = "filters_form",
        ) {
            Column {
                HorizontalDivider()
                ArchiveForm(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Smd),
                    onTypeChange = onTypeChange,
                    onRecordsChange = onRecordsChange,
                    onLoadClick = onLoadClick,
                    onShowStartPicker = onShowStartPicker,
                    onShowEndPicker = onShowEndPicker,
                )
            }
        }
    }
}

/**
 * Кликабельная шапка панели «Фильтры и сортировка».
 *
 * Единый заголовок для карточки фильтров (COMPACT/MEDIUM) и боковой панели
 * (EXPANDED): иконка фильтра, название, вращающийся шеврон раскрытия.
 * В свёрнутом состоянии под названием выводится сводка активных фильтров,
 * чтобы было видно, с какими параметрами загружена таблица.
 *
 * @param expanded Раскрыта ли панель (определяет направление шеврона и сводку).
 * @param state Текущее состояние экрана для сводки фильтров.
 * @param onToggle Колбэк нажатия на шапку.
 * @param modifier Модификатор контейнера шапки.
 */
@Composable
private fun FiltersPanelHeader(
    expanded: Boolean,
    state: ArchiveScreenState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "filters_arrow_rotation",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = if (expanded) "Скрыть фильтры" else "Показать фильтры",
                onClick = onToggle,
            )
            .padding(horizontal = Spacing.Smd, vertical = Spacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(IconSize.Inline),
        )
        Spacer(modifier = Modifier.width(Spacing.Sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Фильтры и сортировка",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!expanded) {
                Text(
                    text = filtersSummary(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.Sm))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = if (expanded) "Скрыть фильтры" else "Показать фильтры",
            modifier = Modifier
                .size(IconSize.Md)
                .rotate(rotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Собирает короткую сводку активных фильтров для свёрнутой шапки.
 *
 * @param state Текущее состояние экрана.
 * @return Строка вида «Тип архива · период · N зап.»; незаполненные даты
 *         показываются многоточием, полностью пустой период — «период не задан».
 */
private fun filtersSummary(state: ArchiveScreenState): String {
    val period = if (state.startDate.isBlank() && state.endDate.isBlank()) {
        "период не задан"
    } else {
        "${state.startDate.ifBlank { "…" }} — ${state.endDate.ifBlank { "…" }}"
    }
    return "${state.selectedType.title} · $period · ${state.records} зап."
}

/**
 * Область вывода результата: индикатор загрузки, ошибка либо таблица с пагинацией.
 *
 * @param state Текущее состояние экрана.
 * @param modifier Модификатор контейнера.
 * @param onPreviousPage Переход на предыдущую страницу.
 * @param onNextPage Переход на следующую страницу.
 * @param onClearError Сброс сообщения об ошибке.
 */
@Composable
private fun ArchiveContent(
    state: ArchiveScreenState,
    modifier: Modifier = Modifier,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onClearError: () -> Unit,
) {
    Column(modifier = modifier) {
        if (state.isLoading && state.rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(ComponentSize.ProgressLg))
            }
        } else if (state.error != null && state.rows.isEmpty()) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.Smd),
                fontSize = AppFontSize.Body,
            )
        } else {
            ArchiveTable(
                type = state.selectedType,
                rows = state.rows,
                isLoadingPage = state.isLoadingPage,
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                modifier = Modifier.weight(1f),
            )
        }
        if (state.error != null && state.rows.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.padding(top = Spacing.Xs),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                action = {
                    TextButton(onClick = onClearError) {
                        Text("OK", fontSize = AppFontSize.Caption)
                    }
                },
            ) {
                Text(state.error!!, fontSize = AppFontSize.Caption)
            }
        }
    }
}

/**
 * Форма фильтров архива. На [WindowSize.MEDIUM] поля дат размещаются в две колонки.
 *
 * @param state Текущее состояние экрана.
 * @param modifier Модификатор контейнера формы.
 * @param onTypeChange Смена типа архива.
 * @param onRecordsChange Смена количества записей на странице.
 * @param onLoadClick Запуск загрузки архива.
 * @param onShowStartPicker Открытие диалога выбора даты начала.
 * @param onShowEndPicker Открытие диалога выбора даты окончания.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveForm(
    state: ArchiveScreenState,
    modifier: Modifier = Modifier,
    onTypeChange: (ArchiveType) -> Unit,
    onRecordsChange: (String) -> Unit,
    onLoadClick: () -> Unit,
    onShowStartPicker: () -> Unit,
    onShowEndPicker: () -> Unit,
) {
    val windowSize = rememberWindowSize()
    var typeExpanded by remember { mutableStateOf(false) }
    var recordsExpanded by remember { mutableStateOf(false) }
    val recordsOptions = listOf("25", "50", "100", "200")

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded },
        ) {
            OutlinedTextField(
                value = state.selectedType.title,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип архива", fontSize = AppFontSize.Body) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .clip(AppShape.Medium),
                shape = AppShape.Medium,
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                    fontSize = AppFontSize.Field,
                ),
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false },
            ) {
                ArchiveType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.title, fontSize = AppFontSize.Field) },
                        onClick = {
                            onTypeChange(type)
                            typeExpanded = false
                        },
                    )
                }
            }
        }

        if (windowSize == WindowSize.MEDIUM) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
                DateField(
                    label = "Дата и время начала",
                    value = state.startDate,
                    onClick = onShowStartPicker,
                    modifier = Modifier.weight(1f),
                )
                DateField(
                    label = "Дата и время окончания",
                    value = state.endDate,
                    onClick = onShowEndPicker,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            DateField(
                "Дата и время начала",
                state.startDate,
                onShowStartPicker,
                Modifier.fillMaxWidth(),
            )
            DateField(
                "Дата и время окончания",
                state.endDate,
                onShowEndPicker,
                Modifier.fillMaxWidth(),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.Xsm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExposedDropdownMenuBox(
                expanded = recordsExpanded,
                onExpandedChange = { recordsExpanded = !recordsExpanded },
            ) {
                OutlinedTextField(
                    value = state.records,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = recordsExpanded)
                    },
                    modifier = Modifier
                        .width(ComponentSize.RecordsFieldWidth)
                        .height(ComponentSize.RecordsFieldHeight)
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .clip(AppShape.Medium),
                    shape = AppShape.Medium,
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                        fontSize = AppFontSize.Field,
                    ),
                )
                ExposedDropdownMenu(
                    expanded = recordsExpanded,
                    onDismissRequest = { recordsExpanded = false },
                ) {
                    recordsOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = AppFontSize.Field) },
                            onClick = {
                                onRecordsChange(option)
                                recordsExpanded = false
                            },
                        )
                    }
                }
            }
            Button(
                onClick = onLoadClick,
                enabled = !state.isLoading,
                modifier = Modifier
                    .height(ComponentSize.ButtonHeight)
                    .weight(1f)
                    .clip(AppShape.Medium),
                shape = AppShape.Medium,
                contentPadding = PaddingValues(horizontal = Spacing.Smd),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(IconSize.Sm),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        "Загрузить",
                        fontSize = AppFontSize.Field,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/**
 * Подписанное поле выбора даты/времени периода.
 *
 * @param label Подпись над полем.
 * @param value Текущее значение даты (пустое — показывается плейсхолдер).
 * @param onClick Открытие диалога выбора.
 * @param modifier Модификатор контейнера поля.
 */
@Composable
private fun DateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = AppFontSize.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PickerFieldButton(
            value = value.ifBlank { "дд.мм.гггг чч:мм" },
            icon = Icons.Default.CalendarToday,
            onClick = onClick,
            shape = AppShape.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Описание колонки таблицы: заголовок и базовая ширина.
 *
 * @property title Заголовок колонки.
 * @property width Базовая ширина колонки (используется и как вес при растяжении).
 */
private data class TableColumn(
    val title: String,
    val width: Dp,
)

/**
 * Возвращает описание колонок таблицы для выбранного типа архива.
 * Ширины колонок — данные конкретного экрана, поэтому заданы локально.
 *
 * @param type Тип архива.
 * @return Список колонок с заголовками и базовыми ширинами.
 */
private fun tableColumns(type: ArchiveType): List<TableColumn> =
    when (type) {
        ArchiveType.EVENTS -> {
            listOf(
                TableColumn("Сотрудник", 150.dp),
                TableColumn("Оборудование", 200.dp),
                TableColumn("Упр. служба", 150.dp),
                TableColumn("Место", 150.dp),
                TableColumn("Класс", 120.dp),
                TableColumn("Текст", 250.dp),
            )
        }
        ArchiveType.HAND_LOADS -> {
            listOf(
                TableColumn("Место", 150.dp),
                TableColumn("Статус", 120.dp),
                TableColumn("Оборудование", 150.dp),
                TableColumn("Вместимость дозатора, кг", 200.dp),
                TableColumn("Компонент", 150.dp),
            )
        }
    }

/**
 * Извлекает текстовые значения ячеек строки таблицы.
 *
 * @param row Строка архива.
 * @return Список значений ячеек в порядке колонок [tableColumns].
 */
private fun cellValues(row: ArchiveRow): List<String> =
    when (row) {
        is ArchiveRow.EventRow -> {
            listOf(
                row.event.person?.fullName ?: "—",
                row.event.equipment?.name ?: "—",
                row.event.deviceName ?: "—",
                row.event.workPlaceName ?: "—",
                row.event.className ?: "—",
                row.event.text ?: "—",
            )
        }
        is ArchiveRow.HandLoadRow -> {
            listOf(
                row.handLoad.workPlaceName ?: "—",
                row.handLoad.status.toString(),
                row.handLoad.equipment?.name ?: "—",
                row.handLoad.equipmentCapacity.toString(),
                row.handLoad.component?.name ?: "—",
            )
        }
    }

/**
 * Извлекает строковое представление даты события для фиксированной колонки.
 *
 * @param row Строка архива.
 * @return Дата в формате "dd.MM.yyyy HH:mm:ss".
 */
private fun dateText(row: ArchiveRow): String =
    when (row) {
        is ArchiveRow.EventRow -> {
            row.event.date?.format(DateTimeFormatters.dateTimeFormatter) ?: "Дата не указана"
        }
        is ArchiveRow.HandLoadRow -> {
            row.handLoad.date?.format(DateTimeFormatters.dateTimeFormatter) ?: "Дата не указана"
        }
    }

/**
 * Таблица архива с постраничной навигацией.
 *
 * Поведение колонок адаптивное: если доступная ширина экрана не меньше суммарной
 * ширины колонок, колонки растягиваются пропорционально базовым ширинам
 * ([Modifier.weight]); иначе таблица прокручивается горизонтально общим
 * [ScrollState] для шапки и строк. Скрытие панели фильтров автоматически
 * переключает таблицу в режим растяжения, когда ширины начинает хватать.
 *
 * @param type Тип архива, определяющий набор колонок.
 * @param rows Строки текущей страницы.
 * @param isLoadingPage Флаг загрузки страницы (оверлей с индикатором).
 * @param currentPage Номер текущей страницы.
 * @param totalPages Общее количество страниц.
 * @param onPreviousPage Переход на предыдущую страницу.
 * @param onNextPage Переход на следующую страницу.
 * @param modifier Модификатор контейнера таблицы.
 */
@Composable
private fun ArchiveTable(
    type: ArchiveType,
    rows: List<ArchiveRow>,
    isLoadingPage: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val columns = remember(type) { tableColumns(type) }
    val requiredWidth = DateColumnWidth + columns.fold(0.dp) { acc, c -> acc + c.width }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stretch = this.maxWidth >= requiredWidth

        Column(modifier = Modifier.fillMaxWidth()) {
            TableHeader(columns = columns, stretch = stretch, scrollState = scrollState)
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(
                        items = rows,
                        key = { index, row ->
                            when (row) {
                                is ArchiveRow.EventRow -> "event_${row.event.id}_$index"
                                is ArchiveRow.HandLoadRow -> "handload_${row.handLoad.id}_$index"
                            }
                        },
                    ) { _, row ->
                        TableRow(
                            row = row,
                            columns = columns,
                            stretch = stretch,
                            scrollState = scrollState,
                        )
                    }
                }
                // Оверлей лоадера при переключении страниц
                if (isLoadingPage) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = AppAlpha.Scrim),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(ComponentSize.ProgressXl))
                    }
                }
            }
            PaginationRow(
                currentPage = currentPage,
                totalPages = totalPages,
                isLoadingPage = isLoadingPage,
                centered = stretch,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
            )
        }
    }
}

/**
 * Шапка таблицы. В режиме [stretch] колонки растягиваются на всю ширину,
 * иначе прокручиваются горизонтально синхронно со строками.
 *
 * @param columns Описание колонок.
 * @param stretch Режим растяжения колонок.
 * @param scrollState Общий_state горизонтальной прокрутки шапки и строк.
 */
@Composable
private fun TableHeader(
    columns: List<TableColumn>,
    stretch: Boolean,
    scrollState: ScrollState,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = Spacing.Sm),
    ) {
        HeaderCell("Дата и время", Modifier.width(DateColumnWidth))
        if (stretch) {
            columns.forEach { column ->
                HeaderCell(column.title, Modifier.weight(column.width.value))
            }
        } else {
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                columns.forEach { column ->
                    HeaderCell(column.title, Modifier.width(column.width))
                }
            }
        }
    }
}

/**
 * Ячейка шапки таблицы.
 *
 * @param text Заголовок колонки.
 * @param modifier Модификатор, задающий ширину или вес колонки.
 */
@Composable
private fun HeaderCell(
    text: String,
    modifier: Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = Spacing.Sm),
        fontWeight = FontWeight.Bold,
        fontSize = AppFontSize.Caption,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Строка таблицы. Фиксированная колонка даты + ячейки данных,
 * растягиваемые или прокручиваемые в зависимости от режима [stretch].
 *
 * @param row Строка архива.
 * @param columns Описание колонок.
 * @param stretch Режим растяжения колонок.
 * @param scrollState Общий state горизонтальной прокрутки шапки и строк.
 */
@Composable
private fun TableRow(
    row: ArchiveRow,
    columns: List<TableColumn>,
    stretch: Boolean,
    scrollState: ScrollState,
) {
    val values = cellValues(row)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Xsm)) {
        Box(
            modifier = Modifier
                .width(DateColumnWidth)
                .padding(horizontal = Spacing.Sm),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = dateText(row),
                fontSize = AppFontSize.Caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (stretch) {
            values.forEachIndexed { index, text ->
                CellText(text, Modifier.weight(columns[index].width.value))
            }
        } else {
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                values.forEachIndexed { index, text ->
                    CellText(text, Modifier.width(columns[index].width))
                }
            }
        }
    }
}

/**
 * Ячейка данных таблицы.
 *
 * @param text Значение ячейки.
 * @param modifier Модификатор, задающий ширину или вес колонки.
 */
@Composable
private fun CellText(
    text: String,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = Spacing.Sm),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            fontSize = AppFontSize.Caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Компактная панель постраничной навигации: две кнопки-стрелки и счётчик страниц.
 *
 * Отличия от прежней версии:
 * вместо текстовых OutlinedButton высотой 40 dp — icon-only кнопки-шевроны
 * без фона и рамок (визуально лёгкие; фактическая зона нажатия остаётся
 * не меньше 48 dp благодаря enforcement Material 3);
 * вместо подписи «Страница N из M» — короткий счётчик «N / M»;
 * убран дублирующий индикатор загрузки: загрузку страницы уже показывает
 * оверлей таблицы, здесь кнопки просто блокируются на время запроса;
 * уменьшены вертикальные отступы строки.
 *
 * @param currentPage Номер текущей страницы.
 * @param totalPages Общее количество страниц.
 * @param isLoadingPage Флаг загрузки страницы (блокирует кнопки).
 * @param centered Режим центрированного расположения элементов.
 * @param onPreviousPage Переход на предыдущую страницу.
 * @param onNextPage Переход на следующую страницу.
 */
@Composable
private fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    isLoadingPage: Boolean,
    centered: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = Spacing.Xs,
                horizontal = if (centered) Spacing.Md else Spacing.Sm,
            ),
        horizontalArrangement = if (centered) {
            Arrangement.spacedBy(Spacing.Sm, Alignment.CenterHorizontally)
        } else {
            Arrangement.SpaceBetween
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PageArrowButton(
            imageVector = Icons.Filled.ChevronLeft,
            contentDescription = "Предыдущая страница",
            enabled = !isLoadingPage && currentPage > 1,
            onClick = onPreviousPage,
        )
        Text(
            text = "$currentPage / $totalPages",
            fontSize = AppFontSize.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        PageArrowButton(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Следующая страница",
            enabled = !isLoadingPage && currentPage < totalPages,
            onClick = onNextPage,
        )
    }
}

/**
 * Компактная кнопка-стрелка пагинации без фона и рамки.
 *
 * Занимает минимум визуального места; зона нажатия обеспечивается
 * стандартным enforcement Material 3 для icon-кнопок (не меньше 48 dp),
 * поэтому доступность не страдает в отличие от уменьшенных текстовых кнопок.
 *
 * @param imageVector Вектор стрелки (шеврона).
 * @param contentDescription Описание действия для TalkBack.
 * @param enabled Доступность нажатия.
 * @param onClick Колбэк нажатия.
 */
@Composable
private fun PageArrowButton(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(IconSize.Md),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}