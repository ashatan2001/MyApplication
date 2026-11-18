package com.example.myapplication.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.example.myapplication.presentation.util.AppElevation
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
 * Реализован по гайдлайнам Material 3:
 * - вместо блокирующего скрима — один линейный индикатор у верхней кромки
 *   списка на время операции; контент остаётся видимым и прокручиваемым;
 * - отключаются только конфликтующие действия (enabled у контролов),
 *   просмотр и раскрытие карточек доступны всегда;
 * - обновление — pull-to-refresh с сохранением контента на экране;
 * - результаты операций — Snackbar с окраской по типу сообщения;
 * - удаление — через модальный диалог подтверждения (каноничная модальность M3).
 *
 * Действия с заявкой доступны по видимой кнопке «⋮» на строке
 * (тап и long-press сохранены как дополнительные ярлыки):
 * изменение количества, перемещение в очереди, удаление с подтверждением.
 *
 * @param viewModel [QueueViewModel] для управления состоянием экрана.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val state = uiState
    val snackbarHostState = remember { SnackbarHostState() }

    // Цели диалогов; null — диалог закрыт.
    // TODO: перенести в ViewModel, чтобы диалоги переживали смену конфигурации.
    var loadToEdit by remember { mutableStateOf<Load?>(null) }
    var loadToDelete by remember { mutableStateOf<Load?>(null) }
    var moveTarget by remember { mutableStateOf<MoveTarget?>(null) }

    // Кэш последних успешно загруженных данных: по M3 контент не должен
    // исчезать под спиннером при обновлении уже показанного списка.
    var cachedLoads by remember { mutableStateOf<Map<Terminal, List<Load>>?>(null) }
    LaunchedEffect(state) {
        if (state is QueueUiState.Success) {
            cachedLoads = state.loadsByTerminal
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadQueue()
    }

    // Одноразовые события: каждое сообщение несёт собственный признак ошибки.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QueueEvent.ShowMessage -> snackbarHostState.showSnackbar(
                    QueueSnackbarVisuals(message = event.text, isError = event.isError)
                )
            }
        }
    }

    val isRefreshing = state is QueueUiState.Loading && cachedLoads != null
    // Мутации запрещены во время операции и во время обновления (данные устаревают),
    // но просмотр списка не блокируется никогда.
    val actionsEnabled = !isProcessing && state !is QueueUiState.Loading

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = (data.visuals as? QueueSnackbarVisuals)?.isError == true
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseSurface
                    },
                    contentColor = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    },
                    dismissActionContentColor = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    }
                )
            }
        },
        // Insets уже обработаны оболочкой MainShell — не дублируем отступы.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { if (!isProcessing) viewModel.loadQueue() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val displayLoads = when (state) {
                is QueueUiState.Success -> state.loadsByTerminal
                is QueueUiState.Loading -> cachedLoads
                is QueueUiState.Error -> null
            }

            when {
                state is QueueUiState.Error -> QueueErrorState(
                    message = state.message,
                    onRetry = viewModel::loadQueue,
                    modifier = Modifier.align(Alignment.Center)
                )
                // Первичная загрузка: circular по центру загружающегося контейнера (M3).
                displayLoads == null -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                displayLoads.isEmpty() -> QueueEmptyState(
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> TerminalGroupedList(
                    loadsByTerminal = displayLoads,
                    actionsEnabled = actionsEnabled,
                    onEdit = { load -> loadToEdit = load },
                    onMove = { load, maxPosition -> moveTarget = MoveTarget(load, maxPosition) },
                    onDelete = { load -> loadToDelete = load }
                )
            }

            // M3: один линейный индикатор у верхней кромки контейнера
            // на время мутации — без перекрытия контента скримом.
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Выполняется операция с заявкой" }
                )
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
 * Визуальные параметры Snackbar для сообщений очереди.
 *
 * Признак ошибки хранится в самом сообщении, а не в изменяемом состоянии
 * экрана: окраска гарантированно соответствует конкретному сообщению,
 * даже если события приходят подряд.
 *
 * @property message Текст сообщения.
 * @property isError true — сообщение об ошибке: errorContainer, большая длительность.
 * @property actionLabel Подпись действия (не используется).
 * @property duration Длительность показа: ошибки дольше, чтобы их успели прочитать.
 * @property withDismissAction Кнопка закрытия показывается для ошибок.
 */
private data class QueueSnackbarVisuals(
    override val message: String,
    val isError: Boolean,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration =
        if (isError) SnackbarDuration.Long else SnackbarDuration.Short,
    override val withDismissAction: Boolean = isError
) : SnackbarVisuals

/**
 * Список терминалов с раскрывающимися вложенными списками заявок.
 *
 * Прокрутка и раскрытие карточек доступны всегда (просмотр безопасен),
 * состояние раскрытых терминалов переживает смену конфигурации.
 *
 * @param loadsByTerminal Сгруппированные по терминалу заявки.
 * @param actionsEnabled Разрешены ли действия с заявками (false во время операции/обновления).
 * @param onEdit Колбэк открытия диалога изменения заявки.
 * @param onMove Колбэк открытия диалога перемещения (заявка, размер очереди).
 * @param onDelete Колбэк открытия диалога подтверждения удаления.
 */
@Composable
private fun TerminalGroupedList(
    loadsByTerminal: Map<Terminal, List<Load>>,
    actionsEnabled: Boolean,
    onEdit: (Load) -> Unit,
    onMove: (Load, Int) -> Unit,
    onDelete: (Load) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var expandedTerminalIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    val terminalList = remember(loadsByTerminal) { loadsByTerminal.entries.toList() }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.Sm)
    ) {
        itemsIndexed(terminalList, key = { _, entry -> "terminal_${entry.key.id}" }) { index, entry ->
            val (terminal, loads) = entry
            val isExpanded = terminal.id in expandedTerminalIds

            TerminalCard(
                terminal = terminal,
                loads = loads,
                isExpanded = isExpanded,
                onExpandClick = {
                    val willExpand = !isExpanded
                    expandedTerminalIds = if (willExpand) {
                        expandedTerminalIds + terminal.id
                    } else {
                        expandedTerminalIds - terminal.id
                    }
                    if (willExpand) {
                        coroutineScope.launch { lazyListState.animateScrollToItem(index) }
                    }
                },
                actionsEnabled = actionsEnabled,
                onEdit = onEdit,
                onMove = onMove,
                onDelete = onDelete
            )
        }
    }
}

/**
 * Карточка терминала с раскрывающимся списком заявок.
 *
 * Заголовок кликабелен всегда: раскрытие — это просмотр, а не мутация,
 * и не должен блокироваться во время фоновых операций.
 *
 * @param terminal Модель терминала.
 * @param loads Список заявок, привязанных к данному терминалу.
 * @param isExpanded Признак раскрытого состояния карточки.
 * @param onExpandClick Колбэк нажатия на заголовок карточки.
 * @param actionsEnabled Разрешены ли действия с заявками.
 * @param onEdit Колбэк открытия диалога изменения заявки.
 * @param onMove Колбэк открытия диалога перемещения (заявка, размер очереди).
 * @param onDelete Колбэк открытия диалога подтверждения удаления.
 */
@Composable
private fun TerminalCard(
    terminal: Terminal,
    loads: List<Load>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    actionsEnabled: Boolean,
    onEdit: (Load) -> Unit,
    onMove: (Load, Int) -> Unit,
    onDelete: (Load) -> Unit
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
                            actionsEnabled = actionsEnabled,
                            onEdit = { onEdit(load) },
                            onMove = { onMove(load, loads.size) },
                            onDelete = { onDelete(load) }
                        )
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
 * Действия вызываются видимой кнопкой «⋮» или тапом по строке;
 * long-press с виброоткликом сохранён как дополнительный ярлык.
 * Во время операции строка переводится в disabled-состояние штатно
 * (через enabled), что корректно озвучивается сервисами accessibility.
 *
 * @param load Модель данных заявки.
 * @param canMove Доступно ли перемещение (в очереди терминала больше одной заявки).
 * @param actionsEnabled Разрешены ли действия с заявкой.
 * @param onEdit Колбэк открытия диалога изменения заявки.
 * @param onMove Колбэк открытия диалога перемещения.
 * @param onDelete Колбэк открытия диалога подтверждения удаления.
 */
@Composable
private fun LoadRow(
    load: Load,
    canMove: Boolean,
    actionsEnabled: Boolean,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = actionsEnabled,
                onClick = { showMenu = true },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = true
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

        // Видимая кнопка действий: меню доступно не только через long-press.
        Box {
            IconButton(
                onClick = { showMenu = true },
                enabled = actionsEnabled
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
                    enabled = actionsEnabled,
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Переместить в очереди") },
                    leadingIcon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
                    enabled = actionsEnabled && canMove,
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
                    enabled = actionsEnabled,
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
 * Поле количества хранит сырой текст: его можно очистить и ввести дробное
 * значение с любым разделителем; подтверждение блокируется при невалидном
 * значении. Подтверждение доступно и с клавиатуры (ImeAction.Done).
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
        modifier = Modifier.imePadding(),
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
                    supportingText = { if (isError) Text("Введите число больше нуля") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isValid) quantity?.let(onConfirm) }
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
 * Валидирует новую позицию на клиенте: целое число от 1 до размера очереди.
 * Кнопка подтверждения недоступна при невалидном значении; подтверждение
 * доступно и с клавиатуры (ImeAction.Done).
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
        modifier = Modifier.imePadding(),
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
                    supportingText = { if (isError) Text("Введите число от 1 до $maxPosition") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isValid) number.let(onConfirm) }
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
 * Пустое состояние очереди: иконка, заголовок и пояснение с подсказкой
 * о geste обновления. Заменяет «голый» текст по центру экрана.
 *
 * @param modifier Модификатор контейнера.
 */
@Composable
private fun QueueEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(Spacing.Lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Sm)
    ) {
        Icon(
            imageVector = Icons.Default.PendingActions,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(IconSize.Xl)
        )
        Text(
            text = "Очередь заявок пуста",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Новые заявки на отгрузку появятся здесь. Потяните вниз, чтобы обновить.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Состояние ошибки загрузки с кнопкой повтора.
 *
 * @param message Человекочитаемый текст ошибки.
 * @param onRetry Колбэк повторной загрузки.
 * @param modifier Модификатор контейнера.
 */
@Composable
private fun QueueErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Spacing.Lg),
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
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) { Text("Повторить") }
    }
}

/**
 * Визуальные обозначения статусов заявки.
 *
 * contentDescription — человекочитаемая подпись статуса (не enum.name),
 * чтобы TalkBack озвучивал «В очереди», а не «IN_QUEUE».
 *
 * @param status Статус заявки ([LoadStatus]).
 */
@Composable
fun StatusIcon(status: LoadStatus) {
    val (icon, color, label) = when (status) {
        LoadStatus.IN_QUEUE -> Triple(Icons.Default.Schedule, StatusColors.Info, "В очереди")
        LoadStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, StatusColors.Success, "Выполнена")
        LoadStatus.DEFECT -> Triple(Icons.Default.Error, StatusColors.Error, "Брак")
        LoadStatus.CANCELLED -> Triple(Icons.Default.Cancel, StatusColors.Neutral, "Отменена")
        LoadStatus.WEIGHED -> Triple(Icons.Default.Scale, StatusColors.Warning, "Взвешена")
        LoadStatus.UNKNOWN -> Triple(Icons.AutoMirrored.Filled.Help, StatusColors.Maintenance, "Неизвестный статус")
    }

    Icon(
        imageVector = icon,
        contentDescription = label,
        tint = color,
        modifier = Modifier.size(IconSize.Md)
    )
}