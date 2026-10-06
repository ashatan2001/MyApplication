package com.example.presentation.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Просмотр архивов") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (state.error != null) {
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть ошибку")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- 1. Форма запроса ---
            ArchiveForm(
                state = state,
                onTypeChange = viewModel::updateType,
                onStartDateChange = viewModel::updateStartDate,
                onEndDateChange = viewModel::updateEndDate,
                onRecordsChange = viewModel::updateRecords,
                onLoadClick = viewModel::loadArchive
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- 2. Таблица данных ---
            if (state.isLoading && state.rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null && state.rows.isEmpty()) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
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

            // Snackbar для ошибок поверх данных
            if (state.error != null && state.rows.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier.padding(top = 8.dp),
                    action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
                ) {
                    Text(state.error!!)
                }
            }
        }
    }
}

@Composable
private fun ArchiveForm(
    state: ArchiveScreenState,
    onTypeChange: (ArchiveType) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onRecordsChange: (String) -> Unit,
    onLoadClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Выпадающий список
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.selectedType.title,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип архива") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ArchiveType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.title) },
                        onClick = {
                            onTypeChange(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Поля дат (в реальном проекте здесь лучше использовать Material 3 DatePickerDialog)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.startDate,
                onValueChange = onStartDateChange,
                label = { Text("Дата начала") },
                placeholder = { Text("дд.мм.гггг чч:мм:сс") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.endDate,
                onValueChange = onEndDateChange,
                label = { Text("Дата окончания") },
                placeholder = { Text("дд.мм.гггг чч:мм:сс") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = state.records,
                onValueChange = onRecordsChange,
                label = { Text("Записей на стр.") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onLoadClick,
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Загрузить")
                }
            }
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
        // Заголовок таблицы (динамический)
        TableHeader(type)

        // Тело таблицы
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Занимает все доступное пространство
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            items(rows, key = {
                // Генерируем уникальный ключ для перерисовки
                when (it) {
                    is ArchiveRow.EventRow -> "event_${it.event.id}"
                    // is ArchiveRow.HandLoadRow -> "handload_${it.handLoad.id}"
                }
            }) { row ->
                TableRow(row, type)
            }

            // Индикатор подгрузки внизу списка
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Кнопка "Загрузить еще", если включена ручная пагинация,
            // или можно вызывать onLoadMore автоматически при скролле (как в предыдущих примерах)
            if (hasNextPage && !isLoadingMore) {
                item {
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Загрузить следующую страницу")
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (type) {
            ArchiveType.EVENTS -> {
                Text("ID", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                Text("Дата и время", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                Text("Сотрудник", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                Text("Событие", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
            }
            ArchiveType.HAND_LOADS -> {
                Text("ID", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                Text("Дата", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Объем", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Комментарий", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
            }
        }
    }
    Divider(color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun TableRow(row: ArchiveRow, type: ArchiveType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (row) {
            is ArchiveRow.EventRow -> {
                val event = row.event
                Text(event.id.toString(), modifier = Modifier.weight(0.5f))
                Text(
                    event.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                    modifier = Modifier.weight(1.5f)
                )
                Text(event.person?.fullName ?: "—", modifier = Modifier.weight(1.5f))
                Text(event.text ?: "—", modifier = Modifier.weight(2f))
            }
            // is ArchiveRow.HandLoadRow -> { ... аналогично ... }
        }
    }
    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
}