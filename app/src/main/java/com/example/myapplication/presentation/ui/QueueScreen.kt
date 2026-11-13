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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Load
import com.example.domain.model.LoadStatus
import com.example.domain.model.Terminal
import com.example.myapplication.presentation.viewmodel.QueueUiState
import com.example.myapplication.presentation.viewmodel.QueueViewModel
import kotlinx.coroutines.launch

/**
 * Экран очереди заявок на отгрузку, сгруппированных по терминалам.
 * Отображает список терминалов в виде карточек. При нажатии на карточку
 * раскрывается вложенный список заявок этого терминала.
 *
 * @param viewModel [QueueViewModel] для управления состоянием экрана.
 */
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadQueue()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is QueueUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is QueueUiState.Success -> {
                if (state.loadsByTerminal.isEmpty()) {
                    Text(
                        text = "Очередь заявок на отгрузку пуста",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    TerminalGroupedList(
                        loadsByTerminal = state.loadsByTerminal,
                        onEdit = { viewModel.updateLoad(it) },
                        onDelete = { viewModel.deleteLoad(it) },
                        isProcessing = isProcessing
                    )
                }
            }
            is QueueUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
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

        // Глобальный индикатор загрузки поверх экрана при выполнении сетевых действий
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

/**
 * Список терминалов с раскрывающимися вложенными списками заявок.
 * Реализует автоматическую прокрутку к раскрываемому элементу.
 *
 * @param loadsByTerminal Сгруппированные по терминалу заявки.
 * @param onEdit Колбэк для действия "Изменить".
 * @param onDelete Колбэк для действия "Удалить".
 * @param isProcessing Флаг активной сетевой операции для блокировки UI.
 */
@Composable
private fun TerminalGroupedList(
    loadsByTerminal: Map<Terminal, List<Load>>,
    onEdit: (Load) -> Unit,
    onDelete: (Load) -> Unit,
    isProcessing: Boolean
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val expandedTerminals = remember { mutableStateMapOf<String, Boolean>() }

    // Преобразуем Map в List для использования itemsIndexed и получения индекса для прокрутки
    val terminalList = loadsByTerminal.entries.toList()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
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
 * @param onEdit Колбэк действия редактирования заявки.
 * @param onDelete Колбэк действия удаления заявки.
 * @param isProcessing Флаг блокировки взаимодействий во время сетевого запроса.
 */
@Composable
private fun TerminalCard(
    terminal: Terminal,
    loads: List<Load>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onEdit: (Load) -> Unit,
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Заголовок карточки (закрытое состояние)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandClick() }
                    .padding(16.dp),
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

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    modifier = Modifier
                        .size(24.dp)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            onEdit = onEdit,
                            onDelete = onDelete,
                            isProcessing = isProcessing
                        )
                        // Разделитель между заявками, но не после последней
                        if (index < loads.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Строка отдельной заявки в раскрывающемся списке.
 * Поддерживает долгое удержание для вызова контекстного меню (DropdownMenu).
 *
 * @param load Модель данных заявки.
 * @param onEdit Колбэк действия редактирования.
 * @param onDelete Колбэк действия удаления.
 * @param isProcessing Флаг блокировки взаимодействий во время сетевого запроса.
 */
@Composable
private fun LoadRow(
    load: Load,
    onEdit: (Load) -> Unit,
    onDelete: (Load) -> Unit,
    isProcessing: Boolean
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        // Здесь можно добавить логику обычного клика, например, переход на детальный экран
                    },
                    onLongClick = {
                        if (!isProcessing) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    }
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Заявка №${load.id}",
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

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${load.quality} ${load.terminal?.measure ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            StatusIcon(status = load.status)
        }

        // Контекстное меню, привязанное к строке
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Изменить") },
                onClick = {
                    showMenu = false
                    onEdit(load)
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                enabled = !isProcessing
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onDelete(load)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                enabled = !isProcessing
            )
        }
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
        LoadStatus.IN_QUEUE -> Icons.Default.Schedule to Color(0xFF2196F3) // Синий
        LoadStatus.COMPLETED -> Icons.Default.CheckCircle to Color(0xFF4CAF50) // Зеленый
        LoadStatus.DEFECT -> Icons.Default.Error to Color(0xFFF44336) // Красный
        LoadStatus.CANCELLED -> Icons.Default.Cancel to Color(0xFF9E9E9E) // Серый
        LoadStatus.WEIGHED -> Icons.Default.Scale to Color(0xFFFF9800) // Янтарный
        LoadStatus.UNKNOWN -> Icons.AutoMirrored.Filled.Help to Color(0xFF795548) // Коричневый
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}