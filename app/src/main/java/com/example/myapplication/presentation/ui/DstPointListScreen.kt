package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
 *
 * Отображает список доступных зон в виде таблицы с закрепленным заголовком.
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

    LaunchedEffect(Unit) {
        viewModel.loadDstPointList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DstPointListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is DstPointListUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    stickyHeader { TableHeader() }
                    if (state.dstPointList.isEmpty()) {
                        item {
                            Text(
                                text = "Список точек выгрузки бетона пуст",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(items = state.dstPointList, key = { point -> point.zoneId }) { point ->
                            DstPointRow(dstPoint = point, onClick = { onProductList(point.zoneId) })
                            HorizontalDivider()
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
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
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
private fun DstPointRow(dstPoint: DstPoint, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 16.dp, vertical = 6.dp),
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
                modifier = Modifier.size(20.dp),
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
    Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Наименование подразделения",
                modifier = Modifier.weight(7f),
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