package com.example.myapplication.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.ArchiveRow
import com.example.myapplication.presentation.ArchiveScreenState
import com.example.myapplication.presentation.ArchiveType
import com.example.myapplication.presentation.viewmodel.ArchiveViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Просмотр архивов", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

        // Диалог выбора даты и времени начала
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

        // Диалог выбора даты и времени окончания
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

    val recordsOptions = listOf("10", "25", "50", "100", "200")
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
    Column(modifier = Modifier.fillMaxWidth()) {
        TableHeader(type)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            items(rows, key = {
                when (it) {
                    is ArchiveRow.EventRow -> "event_${it.event.id}"
                }
            }) { row ->
                TableRow(row, type)
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
private fun TableHeader(type: ArchiveType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (type) {
            ArchiveType.EVENTS -> {
                Text("ID", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Дата и время", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Сотрудник", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Событие", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            ArchiveType.HAND_LOADS -> {
                Text("ID", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Дата", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Объем", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Комментарий", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TableRow(row: ArchiveRow, type: ArchiveType) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (row) {
            is ArchiveRow.EventRow -> {
                val event = row.event
                Text(event.id.toString(), modifier = Modifier.weight(0.5f), fontSize = 12.sp)
                // Форматирование даты (предполагается, что event.date поддерживает java.time.format или имеет свой toString)
                Text(
                    event.date.toString().replace("T", " "),
                    modifier = Modifier.weight(1.5f),
                    fontSize = 12.sp
                )
                Text(event.person?.fullName ?: "—", modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                Text(event.text ?: "—", modifier = Modifier.weight(2f), fontSize = 12.sp)
            }
        }
    }
}