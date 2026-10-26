package com.example.myapplication.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

/**
 * Экран очереди заявок на отгрузку, сгруппированных по терминалам.
 *
 * Отображает список терминалов. При нажатии на строку терминала
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
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = { viewModel.loadQueue() }) { Text("Повторить") }
                }
            }
        }

        // Глобальный индикатор загрузки поверх экрана при выполнении действий
        if (isProcessing) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

/**
* Список терминалов с раскрывающимися вложенными списками заявок.
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
    val expandedTerminals = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader { TableHeader() }

        loadsByTerminal.forEach { (terminal, loads) ->
            item(key = "terminal_${terminal.id}") {
                TerminalHeader(
                    terminal = terminal,
                    loadsCount = loads.size,
                    isExpanded = expandedTerminals[terminal.id] == true,
                    onClick = {
                        expandedTerminals[terminal.id] = !(expandedTerminals[terminal.id] ?: false)
                    }
                )
            }

            if (expandedTerminals[terminal.id] == true) {
                items(items = loads, key = { load -> load.id }) { load ->
                    LoadRow(
                        load = load,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        isProcessing = isProcessing
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Заголовок группы терминала. При нажатии раскрывает/сворачивает список заявок.
 *
 * @param terminal Модель терминала.
 * @param loadsCount Количество заявок в группе.
 * @param isExpanded Признак раскрытого состояния.
 * @param onClick Колбэк нажатия на заголовок.
 */
@Composable
private fun TerminalHeader(
    terminal: Terminal,
    loadsCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "arrow_rotation"
    )

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = terminal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Заявок: $loadsCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
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
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 12.dp),
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${load.quality} ${load.terminal?.measure ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(16.dp))
                StatusIcon(load.status)
            }
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
 * Закрепленный заголовок таблицы.
 */
@Composable
private fun TableHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Терминал / Заявка",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Визуальные обозначения статусов.
 */
@Composable
fun StatusIcon(status: LoadStatus) {
    val icon = when (status) {
        LoadStatus.IN_QUEUE -> Icons.Default.Schedule
        LoadStatus.COMPLETED -> Icons.Default.CheckCircle
        LoadStatus.DEFECT -> Icons.Default.Error
        LoadStatus.CANCELLED -> Icons.Default.Cancel
        LoadStatus.WEIGHED -> Icons.Default.Scale
        LoadStatus.UNKNOWN -> Icons.AutoMirrored.Filled.Help
    }

    val color = when (status) {
        LoadStatus.IN_QUEUE -> Color(0xFF2196F3)
        LoadStatus.COMPLETED -> Color(0xFF4CAF50)
        LoadStatus.DEFECT -> Color(0xFFF44336)
        LoadStatus.CANCELLED -> Color(0xFF9E9E9E)
        LoadStatus.WEIGHED -> Color(0xFFFF9800)
        LoadStatus.UNKNOWN -> Color(0xFF795548)
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}