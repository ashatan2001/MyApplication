package com.example.myapplication.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Product
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
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
                            .padding(horizontal = Spacing.Md, vertical = Spacing.Sm)
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
                                .padding(Spacing.Lg),
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
                                    modifier = Modifier.padding(horizontal = Spacing.Md, vertical = Spacing.Smd)
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
                                HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Md))
                            }
                        }
                    }
                }
            }
            is ProductListUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.Lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.Md)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(IconSize.Xl)
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
            .heightIn(min = ComponentSize.ListRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Номер продукта
        Text(
            text = product.number ?: "---",
            modifier = Modifier
                .weight(1f)
                .padding(end = Spacing.Smd),
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
                .padding(end = Spacing.Smd),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Иконка заказа (компактная кнопка)
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(IconSize.Action)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Заказать ${product.name}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IconSize.Lg)
            )
        }
    }
}