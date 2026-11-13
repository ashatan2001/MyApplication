package com.example.myapplication.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.domain.model.Product
import com.example.myapplication.presentation.viewmodel.ProductListUiState
import com.example.myapplication.presentation.viewmodel.ProductListViewModel

/**
 * Экран списка продуктов, разрешенных для отгрузки в конкретной зоне.
 * Отображает таблицу продуктов с полем поиска. При нажатии на продукт или иконку заказа
 * открывает диалог создания заявки ([CreateTaskDialog]).
 * Интерфейс оптимизирован: убраны тяжелые карточки и sticky-заголовки.
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
                        text = "Поиск продукта",
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

                    if (filteredList.isEmpty()) {
                        // Состояние пустого списка
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Ничего не найдено по запросу \"$searchQuery\""
                            else
                                "Список продуктов пуст",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {

                            // Легкий заголовок секции
                            item(key = "header") {
                                Text(
                                    text = "Доступные продукты",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                HorizontalDivider()
                            }

                            items(
                                items = filteredList,
                                key = { product -> product.id }
                            ) { product ->
                                ProductRow(
                                    product = product,
                                    onClick = { viewModel.onProductClick(product) }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
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
 * Использует [Modifier.clickable] вместо [androidx.compose.material3.Surface]
 * для уменьшения глубины дерева компоновки и повышения производительности.
 *
 * @param product Модель данных продукта.
 * @param onClick Действие при нажатии на строку или иконку заказа.
 */
@Composable
private fun ProductRow(
    product: Product,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Номер продукта
        Text(
            text = product.number ?: "---",
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
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
                .weight(3f)
                .padding(end = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Иконка заказа (компактная кнопка)
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Заказать ${product.name}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}