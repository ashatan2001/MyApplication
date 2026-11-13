package com.example.myapplication.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.ArchiveRow
import com.example.myapplication.presentation.ArchiveScreenState
import com.example.myapplication.presentation.ArchiveType
import com.example.myapplication.presentation.util.AppAlpha
import com.example.myapplication.presentation.util.AppFontSize
import com.example.myapplication.presentation.util.AppShape
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.DateTimeFormatters
import com.example.myapplication.presentation.util.DateTimeFormatters.parseToMillis
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.LayoutSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.util.WindowSize
import com.example.myapplication.presentation.util.rememberWindowSize
import com.example.myapplication.presentation.viewmodel.ArchiveViewModel

/**
 * Ширина фиксированной колонки с датой в таблице.
 * Специфична для экрана архива, поэтому остаётся локальной константой файла.
 */
private val DateColumnWidth: Dp = 160.dp

/**
 * Экран архива с адаптивной версткой:
 * [WindowSize.COMPACT] — форма над таблицей, колонки таблицы с горизонтальной прокруткой;
 * [WindowSize.MEDIUM] — то же, но поля дат размещаются в две колонки для экономии высоты;
 * [WindowSize.EXPANDED] — форма в боковой панели слева, таблица занимает оставшуюся ширину,
 * колонки таблицы растягиваются пропорционально без прокрутки, если хватает места.
 *
 * @param viewModel [ArchiveViewModel] для управления состоянием экрана.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val windowSize = rememberWindowSize()
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    when (windowSize) {
        // Планшет ландшафт: форма слева, таблица справа
        WindowSize.EXPANDED -> Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Md)
        ) {
            ArchiveForm(
                state = state,
                modifier = Modifier
                    .width(LayoutSize.FormSidePanelWidth) // единая ширина боковых форм
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                onTypeChange = viewModel::updateType,
                onRecordsChange = viewModel::updateRecords,
                onLoadClick = viewModel::loadArchive,
                onShowStartPicker = { showStartPicker = true },
                onShowEndPicker = { showEndPicker = true }
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            ArchiveContent(
                state = state,
                modifier = Modifier.weight(1f),
                onPreviousPage = viewModel::previousPage,
                onNextPage = viewModel::nextPage,
                onClearError = viewModel::clearError
            )
        }
        // Телефон и планшет портрет: форма над таблицей
        else -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.Smd, vertical = Spacing.Sm)
        ) {
            ArchiveForm(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                onTypeChange = viewModel::updateType,
                onRecordsChange = viewModel::updateRecords,
                onLoadClick = viewModel::loadArchive,
                onShowStartPicker = { showStartPicker = true },
                onShowEndPicker = { showEndPicker = true }
            )
            Spacer(modifier = Modifier.height(Spacing.Sm))
            ArchiveContent(
                state = state,
                modifier = Modifier.weight(1f),
                onPreviousPage = viewModel::previousPage,
                onNextPage = viewModel::nextPage,
                onClearError = viewModel::clearError
            )
        }
    }

    if (showStartPicker) {
        NativeDateTimePickerDialog(
            initialDateMillis = parseToMillis(state.startDate),
            onDismiss = { showStartPicker = false },
            onDateTimeSelected = { formattedString ->
                viewModel.updateStartDate(formattedString)
                showStartPicker = false
            }
        )
    }
    if (showEndPicker) {
        NativeDateTimePickerDialog(
            initialDateMillis = parseToMillis(state.endDate),
            onDismiss = { showEndPicker = false },
            onDateTimeSelected = { formattedString ->
                viewModel.updateEndDate(formattedString)
                showEndPicker = false
            }
        )
    }
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
    onClearError: () -> Unit
) {
    Column(modifier = modifier) {
        if (state.isLoading && state.rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(ComponentSize.ProgressLg))
            }
        } else if (state.error != null && state.rows.isEmpty()) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(Spacing.Smd),
                fontSize = AppFontSize.Body
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
                modifier = Modifier.weight(1f)
            )
        }
        if (state.error != null && state.rows.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.padding(top = Spacing.Xs),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                action = { TextButton(onClick = onClearError) { Text("OK", fontSize = AppFontSize.Caption) } }
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
    onShowEndPicker: () -> Unit
) {
    val windowSize = rememberWindowSize()
    var typeExpanded by remember { mutableStateOf(false) }
    var recordsExpanded by remember { mutableStateOf(false) }
    val recordsOptions = listOf("25", "50", "100", "200")

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
            OutlinedTextField(
                value = state.selectedType.title,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип архива", fontSize = AppFontSize.Body) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .clip(AppShape.Medium),
                shape = AppShape.Medium,
                textStyle = LocalTextStyle.current.copy(fontSize = AppFontSize.Field)
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                ArchiveType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.title, fontSize = AppFontSize.Field) },
                        onClick = { onTypeChange(type); typeExpanded = false }
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
                    modifier = Modifier.weight(1f)
                )
                DateField(
                    label = "Дата и время окончания",
                    value = state.endDate,
                    onClick = onShowEndPicker,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            DateField("Дата и время начала", state.startDate, onShowStartPicker, Modifier.fillMaxWidth())
            DateField("Дата и время окончания", state.endDate, onShowEndPicker, Modifier.fillMaxWidth())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Xsm), verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(expanded = recordsExpanded, onExpandedChange = { recordsExpanded = !recordsExpanded }) {
                OutlinedTextField(
                    value = state.records,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recordsExpanded) },
                    modifier = Modifier
                        .width(ComponentSize.RecordsFieldWidth)
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .clip(AppShape.Medium),
                    shape = AppShape.Medium,
                    textStyle = LocalTextStyle.current.copy(fontSize = AppFontSize.Field)
                )
                ExposedDropdownMenu(expanded = recordsExpanded, onDismissRequest = { recordsExpanded = false }) {
                    recordsOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = AppFontSize.Field) },
                            onClick = { onRecordsChange(option); recordsExpanded = false }
                        )
                    }
                }
            }
            Button(
                onClick = onLoadClick,
                enabled = !state.isLoading,
                modifier = Modifier.height(ComponentSize.ButtonHeight).weight(1f).clip(AppShape.Medium),
                shape = AppShape.Medium,
                contentPadding = PaddingValues(horizontal = Spacing.Smd)
            ) {
                if (state.isLoading) {
                    // Компактный индикатор внутри кнопки — размер иконки кнопки.
                    CircularProgressIndicator(modifier = Modifier.size(IconSize.Sm), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Загрузить", fontSize = AppFontSize.Field, fontWeight = FontWeight.Medium)
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = AppFontSize.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PickerFieldButton(
            value = value.ifBlank { "дд.мм.гггг чч:мм" },
            icon = Icons.Default.CalendarToday,
            onClick = onClick,
            shape = AppShape.Medium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Описание колонки таблицы: заголовок и базовая ширина. */
private data class TableColumn(val title: String, val width: Dp)

/**
 * Возвращает описание колонок таблицы для выбранного типа архива.
 * Ширины колонок — данные конкретного экрана, поэтому заданы локально.
 *
 * @param type Тип архива.
 * @return Список колонок с заголовками и базовыми ширинами.
 */
private fun tableColumns(type: ArchiveType): List<TableColumn> = when (type) {
    ArchiveType.EVENTS -> listOf(
        TableColumn("Сотрудник", 150.dp),
        TableColumn("Оборудование", 200.dp),
        TableColumn("Упр. служба", 150.dp),
        TableColumn("Место", 150.dp),
        TableColumn("Класс", 120.dp),
        TableColumn("Текст", 250.dp)
    )
    ArchiveType.HAND_LOADS -> listOf(
        TableColumn("Место", 150.dp),
        TableColumn("Статус", 120.dp),
        TableColumn("Оборудование", 150.dp),
        TableColumn("Вместимость дозатора, кг", 200.dp),
        TableColumn("Компонент", 150.dp)
    )
}

/**
 * Извлекает текстовые значения ячеек строки таблицы.
 *
 * @param row Строка архива.
 * @return Список значений ячеек в порядке колонок [tableColumns].
 */
private fun cellValues(row: ArchiveRow): List<String> = when (row) {
    is ArchiveRow.EventRow -> listOf(
        row.event.person?.fullName ?: "—",
        row.event.equipment?.name ?: "—",
        row.event.deviceName ?: "—",
        row.event.workPlaceName ?: "—",
        row.event.className ?: "—",
        row.event.text ?: "—"
    )

    is ArchiveRow.HandLoadRow -> listOf(
        row.handLoad.workPlaceName ?: "—",
        row.handLoad.status.toString(),
        row.handLoad.equipment?.name ?: "—",
        row.handLoad.equipmentCapacity.toString(),
        row.handLoad.component?.name ?: "—"
    )
}

/**
 * Извлекает строковое представление даты события для фиксированной колонки.
 *
 * @param row Строка архива.
 * @return Дата в формате "дд.мм.гггг чч:мм:сс".
 */
private fun dateText(row: ArchiveRow): String {
    val formatter = DateTimeFormatters.dateTimeFormatter
    return when (row) {
        is ArchiveRow.EventRow -> {
            row.event.date?.format(formatter) ?: "Дата не указана"
        }
        is ArchiveRow.HandLoadRow -> {
            row.handLoad.date?.format(formatter) ?: "Дата не указана"
        }
    }
}

/**
 * Таблица архива с постраничной навигацией.
 * Поведение колонок адаптивное: если доступная ширина экрана не меньше суммарной
 * ширины колонок, колонки растягиваются пропорционально базовым ширинам ([Modifier.weight]);
 * иначе таблица прокручивается горизонтально общим [ScrollState] для шапки и строк.
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
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val columns = remember(type) { tableColumns(type) }
    val requiredWidth = DateColumnWidth + columns.fold(0.dp) { acc, column -> acc + column.width }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stretch = this.maxWidth >= requiredWidth

        Column(modifier = Modifier.fillMaxWidth()) {
            TableHeader(columns = columns, stretch = stretch, scrollState = scrollState)
            HorizontalDivider()

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(
                        items = rows,
                        key = { index, row ->
                            when (row) {
                                is ArchiveRow.EventRow -> "event_${row.event.id}_$index"
                                is ArchiveRow.HandLoadRow -> "handload_${row.handLoad.id}_$index"
                            }
                        }
                    ) { _, row ->
                        TableRow(row = row, columns = columns, stretch = stretch, scrollState = scrollState)
                    }
                }

                // Оверлей лоадера при переключении страниц
                if (isLoadingPage) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = AppAlpha.Scrim)),
                        contentAlignment = Alignment.Center
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
                onNextPage = onNextPage
            )
        }
    }
}

/**
 * Шапка таблицы. В режиме [stretch] колонки растягиваются на всю ширину,
 * иначе прокручиваются горизонтально синхронно со строками.
 */
@Composable
private fun TableHeader(
    columns: List<TableColumn>,
    stretch: Boolean,
    scrollState: ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = Spacing.Sm)
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
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = Spacing.Sm),
        fontWeight = FontWeight.Bold,
        fontSize = AppFontSize.Caption,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Строка таблицы. Фиксированная колонка даты + ячейки данных,
 * растягиваемые или прокручиваемые в зависимости от режима [stretch].
 */
@Composable
private fun TableRow(
    row: ArchiveRow,
    columns: List<TableColumn>,
    stretch: Boolean,
    scrollState: ScrollState
) {
    val values = cellValues(row)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Xsm)) {
        Box(
            modifier = Modifier.width(DateColumnWidth).padding(horizontal = Spacing.Sm),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = dateText(row),
                fontSize = AppFontSize.Caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun CellText(text: String, modifier: Modifier) {
    Box(modifier = modifier.padding(horizontal = Spacing.Sm), contentAlignment = Alignment.CenterStart) {
        Text(text = text, fontSize = AppFontSize.Caption, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Панель постраничной навигации.
 * На широких экранах ([centered] = true) элементы группируются по центру,
 * на узких — разводятся по краям.
 *
 * @param currentPage Номер текущей страницы.
 * @param totalPages Общее количество страниц.
 * @param isLoadingPage Флаг загрузки страницы.
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
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Sm, horizontal = if (centered) Spacing.Md else 0.dp),
        horizontalArrangement = if (centered) {
            Arrangement.spacedBy(Spacing.Lg, Alignment.CenterHorizontally)
        } else {
            Arrangement.SpaceBetween
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPreviousPage,
            enabled = !isLoadingPage && currentPage > 1,
            modifier = Modifier.height(ComponentSize.SmallButtonHeight)
        ) {
            Text("Назад", fontSize = AppFontSize.Body)
        }
        if (isLoadingPage) {
            CircularProgressIndicator(
                modifier = Modifier.size(ComponentSize.ProgressMd),
                strokeWidth = ComponentSize.ProgressStrokeThin
            )
        } else {
            Text(
                text = "Страница $currentPage из $totalPages",
                fontSize = AppFontSize.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(
            onClick = onNextPage,
            enabled = !isLoadingPage && currentPage < totalPages,
            modifier = Modifier.height(ComponentSize.SmallButtonHeight)
        ) {
            Text("Вперед", fontSize = AppFontSize.Body)
        }
    }
}