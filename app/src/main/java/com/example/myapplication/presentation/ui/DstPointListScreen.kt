package com.example.myapplication.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DstPoint
import com.example.myapplication.presentation.viewmodel.DstPointListUiState
import com.example.myapplication.presentation.viewmodel.DstPointListViewModel

/**
 * Экран списка точек выгрузки бетона.
 * Отображает список доступных зон в виде таблицы с закрепленным заголовком и полем поиска.
 * Удален собственный [Scaffold], так как навигация и заголовок предоставляются [MainShell].
 *
 * @param onProductList Колбэк, вызываемый при выборе точки выгрузки, передает ID зоны.
 * @param viewModel [DstPointListViewModel] для управления загрузкой списка точек.
 */
@Composable
fun DstPointListScreen(
    onProductList: (Int) -> Unit,
    viewModel: DstPointListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadDstPointList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DstPointListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is DstPointListUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Поле поиска
                    SearchField(
                        text = "Наименование подразделения",
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    // Фильтрация списка (исправлено дублирование объявления)
                    val filteredList = state.dstPointList.filter { point ->
                        point.zoneName.contains(searchQuery.trim(), ignoreCase = true)
                    }

                    // Отображение списка
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        stickyHeader { TableHeader() }

                        if (filteredList.isEmpty()) {
                            item {
                                Text(
                                    text = if (searchQuery.isNotEmpty())
                                        "Ничего не найдено по запросу \"$searchQuery\""
                                    else
                                        "Список точек выгрузки бетона пуст",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(items = filteredList, key = { point -> point.zoneId }) { point ->
                                TerminalRow(dstPoint = point, onClick = { onProductList(point.zoneId) })
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            is DstPointListUiState.Error -> {
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
                    Button(onClick = { viewModel.loadDstPointList() }) { Text("Повторить") }
                }
            }
        }
    }
}


/**
 * Отдельная строка списка точек выгрузки.
 * Реагирует на нажатие и вызывает переданный колбэк.
 *
 * @param dstPoint Модель данных точки выгрузки.
 * @param onClick Действие при нажатии на строку.
 */
@Composable
private fun TerminalRow(dstPoint: DstPoint, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dstPoint.zoneName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Закрепленный заголовок таблицы списка точек выгрузки.
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
                text = "Наименование подразделения",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}