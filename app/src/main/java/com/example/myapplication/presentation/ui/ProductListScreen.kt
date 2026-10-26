package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Product
import com.example.myapplication.presentation.viewmodel.ProductListUiState
import com.example.myapplication.presentation.viewmodel.ProductListViewModel

/**
 * Экран списка продуктов, разрешенных для отгрузки в конкретной зоне.
 * Отображает таблицу продуктов с полем поиска. При нажатии на продукт или иконку заказа
 * открывает диалог создания заявки ([CreateTaskDialog]).
 * Удален собственный [Scaffold], навигация "Назад" обрабатывается глобальной панелью [MainShell].
 *
 * @param onBack Колбэк для возврата на предыдущий экран (используется [MainShell]).
 * @param zone ID зоны выгрузки, для которой загружается список продуктов.
 * @param viewModel [ProductListViewModel] для управления состоянием экрана.
 */
@Composable
fun ProductListScreen(
    onBack: () -> Unit,
    zone: Int,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadProductList(zone)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProductListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ProductListUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Поле поиска
                    SearchField(
                        text = "Наименование продукта",
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Фильтрация списка
                    val filteredList = state.productList.filter { product ->
                        product.name.contains(searchQuery.trim(), ignoreCase = true) ||
                                product.number?.contains(searchQuery.trim(), ignoreCase = true) == true
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
                                        "Список продуктов пуст",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(
                                items = filteredList,
                                key = { product -> product.id }
                            ) { product ->
                                ProductRow(
                                    product = product,
                                    onClick = { viewModel.onProductClick(product) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            is ProductListUiState.Error -> {
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
                    Button(onClick = { viewModel.loadProductList(zone) }) {
                        Text("Повторить")
                    }
                }
            }
        }

        // Диалог создания заявки
        selectedProduct?.let { product ->
            CreateTaskDialog(
                product = product,
                zone = zone,
                onDismiss = { viewModel.onDialogDismiss() },
                onSubmit = { quantity, pallete ->
                    viewModel.submitApplication(zone, product, quantity, pallete)
                }
            )
        }
    }
}

/**
 * Отдельная строка списка продуктов с иконкой заказа.
 * При нажатии на строку или иконку вызывает колбэк для открытия диалога создания заявки.
 *
 * @param product Модель данных продукта.
 * @param onClick Действие при нажатии на строку или иконку заказа.
 */
@Composable
private fun ProductRow(product: Product, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер продукта
            Text(
                text = product.number?: "---",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Наименование продукта
            Text(
                text = product.name,
                modifier = Modifier
                    .weight(4f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Иконка заказа
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Заказать ${product.name}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Закрепленный заголовок таблицы списка продуктов.
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
                text = "Номер",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Наименование",
                modifier = Modifier
                    .weight(4f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}