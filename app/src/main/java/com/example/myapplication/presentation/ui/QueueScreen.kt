package com.example.myapplication.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Load
import com.example.domain.model.LoadStatus
import com.example.domain.model.Terminal
import com.example.myapplication.presentation.util.AppAlpha
import com.example.myapplication.presentation.util.AppElevation
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.util.StatusColors
import com.example.myapplication.presentation.viewmodel.QueueEvent
import com.example.myapplication.presentation.viewmodel.QueueUiState
import com.example.myapplication.presentation.viewmodel.QueueViewModel
import kotlinx.coroutines.launch

/**
 * Экран очереди заявок на отгрузку, сгруппированных по терминалам.
 *
 * Действия с заявкой доступны по видимой кнопке «⋮» на строке
 * (тап по строке и long-press сохранены как дополнительные ярлыки):
 * изменение количества, перемещение в очереди, удаление с подтверждением.
 * Результаты операций показываются в Snackbar.
 *
 * @param viewModel [QueueViewModel] для управления состоянием экрана.
 */
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Заявка, ожидающая изменения; null — диалог закрыт
    var loadToEdit by remember { mutableStateOf<Load?>(null) }

    // Заявка, ожидающая подтверждения удаления; null — диалог закрыт
    var loadToDelete by remember { mutableStateOf<Load?>(null) }

    // Цель перемещения; null — диалог закрыт
    var moveTarget by remember { mutableStateOf<MoveTarget?>(null) }

    // Признак ошибки для окраски текущего сообщения Snackbar
    var isErrorSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadQueue()
    }

    // Одноразовые события: сообщения о результатах операций
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QueueEvent.ShowMessage -> {
                    isErrorSnackbar = event.isError
                    snackbarHostState.showSnackbar(event.text)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isErrorSnackbar) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseSurface
                    },
                    contentColor = if (isErrorSnackbar) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is QueueUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is QueueUiState.Success -> {
                    if (state.loadsByTerminal.isEmpty()) {
                        Text(
                            text = "Очередь заявок на отгрузку пуста",
                            modifier = Modifier.align(Alignment.Center).padding(Spacing.Md),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        TerminalGroupedList(
                            loadsByTerminal = state.loadsByTerminal,
                            onEdit = { load -> loadToEdit = load },
                            onMove = { load, maxPosition ->
                                moveTarget = MoveTarget(load, maxPosition)
                            },
                            onDelete = { load -> loadToDelete = load },
                            isProcessing = isProcessing
                        )
                    }
                }
                is QueueUiState.Error -> {
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
                        Button(onClick = { viewModel.loadQueue() }) { Text("Повторить") }
                    }
                }
            }

            // Блокирующий оверлей на время сетевых операций
            if (isProcessing) {
                ProcessingOverlay()
            }
        }
    }

    // Диалог изменения количества продукции в заявке
    loadToEdit?.let { load ->
        EditLoadDialog(
            load = load,
            onConfirm = { quantity ->
                loadToEdit = null
                viewModel.updateLoad(load, quantity)
            },
            onDismiss = { loadToEdit = null }
        )
    }

    // Диалог подтверждения удаления
    loadToDelete?.let { load ->
        ConfirmDeleteLoadDialog(
            loadId = load.id,
            onConfirm = {
                loadToDelete = null
                viewModel.deleteLoad(load)
            },
            onDismiss = { loadToDelete = null }
        )
    }

    // Диалог перемещения заявки в очереди
    moveTarget?.let { target ->
        MoveLoadDialog(
            load = target.load,
            maxPosition = target.maxPosition,
            onConfirm = { number ->
                moveTarget = null
                viewModel.moveLoad(target.load, number)
            },
            onDismiss = { moveTarget = null }
        )
    }
}

/**
 * Цель диалога перемещения заявки.
 *
 * @property load Перемещаемая заявка.
 * @property maxPosition Размер очереди терминала — верхняя граница новой позиции.
 */
private data class MoveTarget(val load: Load, val maxPosition: Int)

/**
 * Список терминалов с раскрывающимися вложенными списками заявок.
 * Реализует автоматическую прокрутку к раскрываемому элементу.
 *
 * @param loadsByTerminal Сгруппированные по терминалу заявки.
 * @param onEdit Колбэк открытия диалога изменения заявки.
 * @param onMove Колбэк открытия диалога перемещения (заявка, размер очереди).
 * @param onDelete Колбэк открытия диалога подтверждения удаления.
 * @param isProcessing Флаг активной сетевой операции для блокировки UI.
 */
@Composable
private fun TerminalGroupedList(
    loadsByTerminal: Map<Terminal, List<Load>>,
    onEdit: (Load) -> Unit,
    onMove: (Load, Int) -> Unit,
    onDelete: (Load) -> Unit,
    isProcessing: Boolean
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val expandedTerminals = remember { mutableStateMapOf<String, Boolean>() }

    val terminalList = loadsByTerminal.entries.toList()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.Sm)
    ) {
        itemsIndexed(terminalList, key = { _, entry -> "terminal_${entry.key.id}" }) { index, entry ->
            val (terminal, loads) = entry
            val isExpanded = expandedTerminals[terminal.id] == true

            TerminalCard(
                terminal = terminal,
                loads = loads,
                isExpanded = isExpanded,
                onExpandClick = {
                    val willExpand = !isExpanded
                    expandedTerminals[terminal.id] = willExpand
                    if (willExpand) {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(index)
                        }
                    }
                },
                onEdit = onEdit,
                onMove = onMove,
                onDelete = onDelete,
                isProcessing = isProcessing
            )
        }
    }
}

/**
 * Карточка терминала с раскрывающимся списком заявок.
 *
 * @param terminal Модель терминала.
 * @param loads Список заявок, привязанных к данному терминалу.
 * @param isExpanded Признак раскрытого состояния карточки.
 * @param onExpandClick Колбэк нажатия на заголовок карточки.
 * @param onEdit Колбэк открытия диалога изменения заявки.
 * @param onMove Колбэк открытия диалога перемещения (заявка, размер очереди).
 * @param onDelete Колбэк открытия диалога подтверждения удаления.
 * @param isProcessing Флаг блокировки взаимодействий во время сетевого запроса.
 */
@Composable
private fun TerminalCard(
    terminal: Terminal,
    loads: List<Load>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onEdit: (Load) -> Unit,
    onMove: (Load, Int) -> Unit,
    onDelete: (Load) -> Unit,
    isProcessing: Boolean
) {
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
            // Заголовок карточки (закрытое состояние)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandClick() }
                    .padding(Spacing.Md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = terminal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Заявок в очереди: ${loads.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

            // Раскрытая информация
            if (isExpanded) {
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Sm)
                ) {
                    Text(
                        text = "Список заявок",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    loads.forEachIndexed { index, load ->
                        LoadRow(
                            load = load,
                            canMove = loads.size > 1,
                            onEdit = { onEdit(load) },
                            onMove = { onMove(load, loads.size) },
                            onDelete = { onDelete(load) },
                            isProcessing = isProcessing
                        )
                        // Разделитель между заявками, но не после последней
                        if (index < loads.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Xs))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Строка отдельной заявки в раскрывающемся списке.
 *
 * Действия вызываются видимой кнопкой «⋮» или обычным тапом по строке;
 * long-press с виброоткликом сохранён как дополнительный ярлык.
 *
 * @param load Модель данных заявки.
 * @param canMove Доступно ли перемещение (в очереди терминала больше одной заявки).
 * @param onEdit Колбэк открытия диалога изменения заявки.
 * @param onMove Колбэк открытия диалога перемещения.
 * @param onDelete Колбэк открытия диалога подтверждения удаления.
 * @param isProcessing Флаг блокировки взаимодействий во время сетевого запроса.
 */
@Composable
private fun LoadRow(
    load: Load,
    canMove: Boolean,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    isProcessing: Boolean
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (!isProcessing) showMenu = true },
                onLongClick = {
                    if (!isProcessing) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                }
            )
            .padding(vertical = Spacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildString {
                    append("Заявка №${load.id}")
                    load.queueNum?.let { append(" · позиция $it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = load.product?.name ?: "Без наименования",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(Spacing.Sm))

        Text(
            text = "${load.quality} ${load.terminal?.measure ?: ""}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(Spacing.Sm))

        StatusIcon(status = load.status)

        // Видимая кнопка действий: меню доступно не только через long-press
        Box {
            IconButton(
                onClick = { showMenu = true },
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Действия с заявкой №${load.id}"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Изменить") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    enabled = !isProcessing,
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Переместить в очереди") },
                    leadingIcon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
                    enabled = !isProcessing && canMove,
                    onClick = {
                        showMenu = false
                        onMove()
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    enabled = !isProcessing,
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

/**
 * Диалог изменения количества продукции в заявке.
 *
 * Терминал, продукт и подразделение не редактируются — они берутся
 * из существующей заявки при формировании запроса на сервер
 * (TerminalInfo / ProductInfo / qty / departmentid).
 * Поле количества хранит сырой текст: его можно очистить и ввести
 * дробное значение с любым разделителем; подтверждение блокируется
 * при невалидном значении.
 *
 * @param load Изменяемая заявка.
 * @param onConfirm Колбэк с новым количеством.
 * @param onDismiss Колбэк закрытия диалога без изменений.
 */
@Composable
private fun EditLoadDialog(
    load: Load,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var quantityText by rememberSaveable { mutableStateOf(load.quality.toString()) }
    val quantity = quantityText.replace(',', '.').toFloatOrNull()
    val isValid = quantity != null && quantity > 0f
    val isError = quantityText.isNotBlank() && !isValid
    val measure = load.terminal?.measure

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Заявка №${load.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Smd)) {
                Text(
                    text = load.product?.name ?: "Без наименования",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Терминал: ${load.terminal?.name ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        // Разрешаем только цифры и не более одного разделителя
                        if (input.count { it == '.' || it == ',' } <= 1 &&
                            input.all { it.isDigit() || it == '.' || it == ',' }
                        ) {
                            quantityText = input
                        }
                    },
                    label = { Text(if (measure != null) "Количество, $measure" else "Количество") },
                    isError = isError,
                    supportingText = {
                        if (isError) Text("Введите число больше нуля")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { quantity?.let(onConfirm) },
                enabled = isValid
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/**
 * Диалог перемещения заявки в очереди её терминала.
 *
 * Валидирует новую позицию на клиенте: целое число от 1 до размера
 * очереди. Кнопка подтверждения недоступна при невалидном значении.
 *
 * @param load Перемещаемая заявка (для заголовка и текущей позиции).
 * @param maxPosition Размер очереди терминала — верхняя граница позиции.
 * @param onConfirm Колбэк с выбранной новой позицией.
 * @param onDismiss Колбэк закрытия диалога без изменений.
 */
@Composable
private fun MoveLoadDialog(
    load: Load,
    maxPosition: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var numberText by rememberSaveable { mutableStateOf(load.queueNum?.toString() ?: "1") }
    val number = numberText.toIntOrNull()
    val isValid = number != null && number in 1..maxPosition
    val isError = numberText.isNotBlank() && !isValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Переместить заявку №${load.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Smd)) {
                Text(
                    text = "Текущая позиция: ${load.queueNum ?: "—"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { input ->
                        // Разрешаем только цифры; поле можно полностью очистить
                        if (input.all { it.isDigit() }) numberText = input
                    },
                    label = { Text("Новая позиция (1–$maxPosition)") },
                    isError = isError,
                    supportingText = {
                        if (isError) Text("Введите число от 1 до $maxPosition")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { number?.let(onConfirm) },
                enabled = isValid
            ) {
                Text("Переместить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/**
 * Диалог подтверждения удаления заявки.
 *
 * Защищает от случайного удаления: деструктивное действие выполняется
 * только после явного подтверждения пользователя.
 *
 * @param loadId Идентификатор заявки для отображения в тексте.
 * @param onConfirm Колбэк подтверждения удаления.
 * @param onDismiss Колбэк отказа от удаления.
 */
@Composable
private fun ConfirmDeleteLoadDialog(
    loadId: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
        title = { Text("Удалить заявку №$loadId?") },
        text = { Text("Заявка будет удалена из очереди. Это действие нельзя отменить.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/**
 * Полноэкранный блокирующий оверлей на время сетевой операции.
 *
 * Перехватывает все касания (нажатия и прокрутку) в фазе Initial,
 * поэтому список под ним действительно не реагирует на ввод,
 * в отличие от простого Box с полупрозрачным фоном.
 */
@Composable
private fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = AppAlpha.ScrimStrong))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                            .changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(ComponentSize.ProgressXl),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = ComponentSize.ProgressStrokeThick
        )
    }
}

/**
 * Визуальные обозначения статусов заявки.
 *
 * @param status Статус заявки ([LoadStatus]).
 */
@Composable
fun StatusIcon(status: LoadStatus) {
    val (icon, color) = when (status) {
        LoadStatus.IN_QUEUE -> Icons.Default.Schedule to StatusColors.Info
        LoadStatus.COMPLETED -> Icons.Default.CheckCircle to StatusColors.Success
        LoadStatus.DEFECT -> Icons.Default.Error to StatusColors.Error
        LoadStatus.CANCELLED -> Icons.Default.Cancel to StatusColors.Neutral
        LoadStatus.WEIGHED -> Icons.Default.Scale to StatusColors.Warning
        LoadStatus.UNKNOWN -> Icons.AutoMirrored.Filled.Help to StatusColors.Maintenance
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(IconSize.Md)
    )
}