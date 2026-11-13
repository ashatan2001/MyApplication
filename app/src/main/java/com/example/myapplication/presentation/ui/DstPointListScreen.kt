package com.example.myapplication.presentation.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
 * Отображает список доступных зон в виде легкой таблицы без карточек.
 * Навигация "Назад" обрабатывается глобальной панелью [MainShell].
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
                        text = "Поиск подразделения",
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Фильтрация списка
                    val filteredList = state.dstPointList.filter { point ->
                        point.zoneName.contains(searchQuery.trim(), ignoreCase = true)
                    }

                    if (filteredList.isEmpty()) {
                        // Состояние пустого списка или отсутствия результатов поиска
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Ничего не найдено по запросу \"$searchQuery\""
                            else
                                "Список точек выгрузки бетона пуст",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {

                            // Легкий заголовок секции (в стиле BucketScreen)
                            item(key = "header") {
                                Text(
                                    text = "Подразделения",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                HorizontalDivider()
                            }

                            items(items = filteredList, key = { point -> point.zoneId }) { point ->
                                DstPointRow(
                                    dstPoint = point,
                                    onOrder = { onProductList(point.zoneId) },
                                    onUnload = { /* Логика отгрузки */ }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.loadDstPointList() }) { Text("Повторить") }
                }
            }
        }
    }
}

/**
 * Отдельная строка списка точек выгрузки.
 * Выполнена в минималистичном стиле без использования Surface/Card для снижения нагрузки на UI.
 *
 * @param dstPoint Модель данных точки выгрузки.
 * @param onOrder Действие при нажатии на кнопку заказа.
 * @param onUnload Действие при нажатии на кнопку отгрузки.
 */
@Composable
private fun DstPointRow(
    dstPoint: DstPoint,
    onUnload: () -> Unit,
    onOrder: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Наименование подразделения
        Text(
            text = dstPoint.zoneName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Кнопка отгрузки
        TextButton(
            onClick = onUnload,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Отгрузить",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Кнопка заказа
        TextButton(
            onClick = onOrder,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Заказ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}