package com.example.myapplication.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.myapplication.presentation.viewmodel.ProductListEvent
import com.example.myapplication.presentation.viewmodel.ProductListUiState
import com.example.myapplication.presentation.viewmodel.ProductListViewModel

/**
 * Экран списка продуктов, разрешенных для отгрузки в конкретной зоне.
 *
 * Отображает таблицу продуктов с полем поиска. При нажатии на продукт или иконку заказа
 * открывает диалог создания заявки ([CreateTaskDialog]). Успешное создание
 * подтверждается Snackbar с номером заявки; ошибки создания показываются
 * внутри диалога.
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
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val submitError by viewModel.submitError.collectAsStateWithLifecycle()
    val zoneName by viewModel.zoneName.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadProductList(zone)
    }

    // Одноразовые события: подтверждение создания заявки
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductListEvent.ShowMessage -> snackbarHostState.showSnackbar(
                    ProductListSnackbarVisuals(message = event.text, isError = event.isError)
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = (data.visuals as? ProductListSnackbarVisuals)?.isError == true
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseSurface
                    },
                    contentColor = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    },
                    dismissActionContentColor = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    }
                )
            }
        },
        // Insets уже обработаны оболочкой MainShell — не дублируем отступы.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is ProductListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProductListUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchField(
                            text = "Поиск продукта",
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.Md, vertical = Spacing.Sm)
                        )

                        val filteredList = state.productList.filter { product ->
                            product.name.contains(searchQuery.trim(), ignoreCase = true) ||
                                    product.number?.contains(searchQuery.trim(), ignoreCase = true) == true
                        }

                        if (filteredList.isEmpty()) {
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
                                items(items = filteredList, key = { product -> product.id }) { product ->
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
                    zoneName = zoneName ?: "Зона $zone",
                    isSubmitting = isSubmitting,
                    errorMessage = submitError,
                    onDismiss = { viewModel.onDialogDismiss() },
                    onSubmit = { quantity, pallete ->
                        viewModel.submitApplication(zone, product, quantity, pallete)
                    }
                )
            }
        }
    }
}

/**
 * Визуальные параметры Snackbar для сообщений экрана списка продуктов.
 *
 * Признак ошибки хранится в самом сообщении, а не в изменяемом состоянии
 * экрана: окраска гарантированно соответствует конкретному сообщению.
 *
 * @property message Текст сообщения.
 * @property isError true — сообщение об ошибке: errorContainer, большая длительность.
 * @property actionLabel Подпись действия (не используется).
 * @property duration Длительность показа: ошибки дольше, чтобы их успели прочитать.
 * @property withDismissAction Кнопка закрытия показывается для ошибок.
 */
private data class ProductListSnackbarVisuals(
    override val message: String,
    val isError: Boolean,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration =
        if (isError) SnackbarDuration.Long else SnackbarDuration.Short,
    override val withDismissAction: Boolean = isError
) : SnackbarVisuals

/**
 * Отдельная строка списка продуктов с иконкой заказа.
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
        Text(
            text = product.number ?: "—",
            modifier = Modifier
                .weight(1f)
                .padding(end = Spacing.Smd),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = product.name,
            modifier = Modifier
                .weight(4f)
                .padding(end = Spacing.Smd),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(IconSize.Action)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Заказать ${product.name}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IconSize.Lg)
            )
        }
    }
}