package com.example.myapplication.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.ArchiveRow
import com.example.myapplication.presentation.ArchiveScreenState
import com.example.myapplication.presentation.ArchiveType
import com.example.myapplication.presentation.viewmodel.ArchiveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        ArchiveForm(
            state = state,
            onTypeChange = viewModel::updateType,
            onRecordsChange = viewModel::updateRecords,
            onLoadClick = viewModel::loadArchive,
            onShowStartPicker = { showStartPicker = true },
            onShowEndPicker = { showEndPicker = true }
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading && state.rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else if (state.error != null && state.rows.isEmpty()) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                fontSize = 13.sp
            )
        } else {
            ArchiveTable(
                type = state.selectedType,
                rows = state.rows,
                isLoadingMore = state.isLoadingMore,
                onLoadMore = viewModel::loadNextPage,
                hasNextPage = state.currentPage < state.totalPages
            )
        }

        if (state.error != null && state.rows.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.padding(top = 4.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK", fontSize = 12.sp) } }
            ) {
                Text(state.error!!, fontSize = 12.sp)
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NativeDateTimePickerDialog(
    initialDateMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onDateTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    if (!showTimePicker) {
        // Шаг 1: Выбор даты
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showTimePicker = true
                }) {
                    Text("Далее")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Выберите дату") }
            )
        }
    } else {
        // Шаг 2: Выбор времени
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Выберите время") },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val date = Date(selectedDateMillis)
                    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
                    val dateStr = dateFormatter.format(date)

                    val timeStr = String.format(Locale.US, "%02d:%02d:00", timePickerState.hour, timePickerState.minute)

                    onDateTimeSelected("$dateStr $timeStr")
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }
}

private fun parseToMillis(dateStr: String): Long {
    return try {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        formatter.parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveForm(
    state: ArchiveScreenState,
    onTypeChange: (ArchiveType) -> Unit,
    onRecordsChange: (String) -> Unit,
    onLoadClick: () -> Unit,
    onShowStartPicker: () -> Unit,
    onShowEndPicker: () -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }
    var recordsExpanded by remember { mutableStateOf(false) }

    val recordsOptions = listOf("25", "50", "100", "200")
    val shape = RoundedCornerShape(8.dp)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
            OutlinedTextField(
                value = state.selectedType.title,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип архива", fontSize = 13.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor().clip(shape),
                shape = shape,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                ArchiveType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.title, fontSize = 14.sp) },
                        onClick = { onTypeChange(type); typeExpanded = false }
                    )
                }
            }
        }

        Text("Дата и время начала", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        WheelPickerButton(
            value = state.startDate.ifBlank { "дд.мм.гггг чч:мм:сс" },
            icon = Icons.Default.CalendarToday,
            onClick = onShowStartPicker,
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Дата и время окончания", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        WheelPickerButton(
            value = state.endDate.ifBlank { "дд.мм.гггг чч:мм:сс" },
            icon = Icons.Default.CalendarToday,
            onClick = onShowEndPicker,
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(expanded = recordsExpanded, onExpandedChange = { recordsExpanded = !recordsExpanded }) {
                OutlinedTextField(
                    value = state.records,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recordsExpanded) },
                    modifier = Modifier.width(90.dp).menuAnchor().clip(shape),
                    shape = shape,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                ExposedDropdownMenu(expanded = recordsExpanded, onDismissRequest = { recordsExpanded = false }) {
                    recordsOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 14.sp) },
                            onClick = { onRecordsChange(option); recordsExpanded = false }
                        )
                    }
                }
            }

            Button(
                onClick = onLoadClick,
                enabled = !state.isLoading,
                modifier = Modifier.height(48.dp).weight(1f).clip(shape),
                shape = shape,
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Загрузить", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun WheelPickerButton(
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier
            .height(44.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape = shape)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, fontSize = 13.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ArchiveTable(
    type: ArchiveType,
    rows: List<ArchiveRow>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    hasNextPage: Boolean
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        TableHeader(type = type, scrollState = scrollState)
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(rows, key = {
                when (it) {
                    is ArchiveRow.EventRow -> "event_${it.event.id}"
                    is ArchiveRow.HandLoadRow -> "handload_${it.handLoad.id}"
                }
            }) { row ->
                TableRow(
                    row = row,
                    type = type,
                    scrollState = scrollState
                )
            }
            if (isLoadingMore) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }
            }
            if (hasNextPage && !isLoadingMore) {
                item {
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier.fillMaxWidth().padding(8.dp).clip(RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Загрузить ещё", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader(type: ArchiveType, scrollState: ScrollState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp)
    ) {
        // Фиксированная колонка "Дата"
        HeaderCell("Дата и время", width = 160.dp)

        // Прокручиваемая часть
        Row(
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            when (type) {
                ArchiveType.EVENTS -> {
                    HeaderCell("Сотрудник", 150.dp)
                    HeaderCell("Оборудование", 200.dp)
                    HeaderCell("Упр. служба", 150.dp)
                    HeaderCell("Место", 150.dp)
                    HeaderCell("Класс", 120.dp)
                    HeaderCell("Текст", 250.dp)
                }
                ArchiveType.HAND_LOADS -> {
                    HeaderCell("Место", 150.dp)
                    HeaderCell("Статус", 120.dp)
                    HeaderCell("Оборудование", 150.dp)
                    HeaderCell("Вместимость дозатора, кг", 200.dp)
                    HeaderCell("Компонент", 150.dp)
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TableRow(row: ArchiveRow, type: ArchiveType, scrollState: ScrollState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when (row) {
                    is ArchiveRow.EventRow -> row.event.date.toString().replace("T", " ")
                    is ArchiveRow.HandLoadRow -> row.handLoad.date.toString().replace("T", " ")
                },
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            when (row) {
                is ArchiveRow.EventRow -> {
                    CellText(row.event.person?.fullName ?: "—", 150.dp)
                    CellText(row.event.equipment?.name ?: "—", 200.dp)
                    CellText(row.event.deviceName ?: "—", 150.dp)
                    CellText(row.event.workPlaceName ?: "—", 150.dp)
                    CellText(row.event.className ?: "—", 120.dp)
                    CellText(row.event.text ?: "—", 250.dp)
                }

                is ArchiveRow.HandLoadRow -> {
                    CellText(row.handLoad.workPlaceName ?: "—", 150.dp)
                    CellText(row.handLoad.status.toString(), 150.dp)
                    CellText(row.handLoad.equipment?.name ?: "—", 200.dp)
                    CellText(row.handLoad.equipmentCapacity.toString(), 150.dp)
                    CellText(row.handLoad.component?.name ?: "—", 200.dp)
                }
            }
        }
    }
}

@Composable
private fun CellText(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}