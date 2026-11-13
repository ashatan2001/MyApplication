package com.example.myapplication.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Moving
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Bucket
import com.example.domain.model.BucketStatus
import com.example.myapplication.presentation.util.DateTimeFormatters
import com.example.myapplication.presentation.viewmodel.BucketUiState
import com.example.myapplication.presentation.viewmodel.BucketViewModel
import kotlinx.coroutines.launch


/**
 * Экран мониторинга бетоновозных телег.
 * Отображает список телег с возможностью раскрытия детальной информации по каждой.
 *
 * @param viewModel ViewModel для управления состоянием экрана.
 */
@Composable
fun BucketScreen(
    viewModel: BucketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /**
     * Инициирует загрузку списка телег при первом запуске экрана.
     * Блок выполняется один раз при вхождении в композицию благодаря ключу [Unit].
     */
    LaunchedEffect(Unit) {
        viewModel.loadBucketList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is BucketUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is BucketUiState.Success -> {
                if (state.bucketList.isEmpty()) {
                    Text(
                        text = "Бетоновозные телеги не найдены",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    BucketList(buckets = state.bucketList)
                }
            }
            is BucketUiState.Error -> {
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
                    Button(onClick = { viewModel.loadBucketList() }) { Text("Повторить") }
                }
            }
        }
    }
}

/**
 * Список бетоновозных телег с возможностью раскрытия деталей и автоматической прокруткой.
 *
 * @param buckets Список моделей телег.
 */
@Composable
private fun BucketList(buckets: List<Bucket>) {
    // Состояние прокрутки для LazyColumn
    val lazyListState = rememberLazyListState()
    // Область корутин для выполнения suspend-функции прокрутки
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Используем itemsIndexed, чтобы знать позицию элемента для прокрутки
        itemsIndexed(buckets, key = { _, bucket -> bucket.id }) { index, bucket ->
            BucketItem(
                bucket = bucket,
                onExpandRequest = {
                    // При запросе на раскрытие плавно прокручиваем список к этому индексу
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(index)
                    }
                }
            )
        }
    }
}

/**
 * Элемент списка бетоновозной телеги.
 * В закрытом состоянии отображает имя, статус и флаг загруженности.
 * В раскрытом состоянии отображает детальную информацию.
 *
 * @param bucket Модель бетоновозной телеги.
 * @param onExpandRequest Колбэк, вызываемый при раскрытии элемента для инициирования прокрутки.
 */
@Composable
private fun BucketItem(
    bucket: Bucket,
    onExpandRequest: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val willExpand = !isExpanded
                        isExpanded = willExpand
                        // Вызываем прокрутку только если элемент раскрывается, а не сворачивается
                        if (willExpand) {
                            onExpandRequest()
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bucket.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                IsFullIcon(isFull = bucket.isFull)
                Spacer(modifier = Modifier.width(16.dp))
                BucketStatusIcon(status = bucket.status)
                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Раскрытая информация
            if (isExpanded) {
                HorizontalDivider()
                ExpandedBucketContent(bucket = bucket)
            }
        }
    }
}

/**
 * Иконка, отображающая флаг загруженности телеги.
 *
 * @param isFull true, если телега загружена, false - если пуста.
 */
@Composable
private fun IsFullIcon(isFull: Boolean) {
    val (icon, color) = if (isFull) {
        Icons.Default.Inventory2 to Color(0xFF4CAF50) // Зеленый (загружена)
    } else {
        Icons.Outlined.Inventory2 to Color(0xFF9E9E9E) // Серый (пуста)
    }

    Icon(
        imageVector = icon,
        contentDescription = if (isFull) "Загружена" else "Пуста",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

/**
 * Детальная информация о телеге в раскрытом состоянии.
 *
 * @param bucket Модель бетоновозной телеги.
 */
@Composable
private fun ExpandedBucketContent(bucket: Bucket) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1) Местоположения
        Text(
            text = "Местоположение",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        InfoRow(label = "Текущий участок", value = bucket.zoneNumber.toString())
        InfoRow(label = "Участок назначения", value = bucket.dstZone.toString())

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // 2) Информация о заявке
        Text(
            text = "Информация о заявке",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        InfoRow(label = "Код заявки", value = if (bucket.loadId != 0L) bucket.loadId.toString() else "Нет активной заявки")

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // 3) Временные отметки
        Text(
            text = "Временные отметки",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        InfoRow(
            label = "Приготовление смеси",
            value = bucket.loadDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
        InfoRow(
            label = "Прибытие в точку разгрузки",
            value = bucket.arrivalDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
        InfoRow(
            label = "Завершение разгрузки",
            value = bucket.departDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
    }
}

/**
 * Строка для отображения пары "метка-значение" в раскрытом списке.
 *
 * @param label Название поля.
 * @param value Значение поля.
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Визуальные обозначения статусов бетоновозной телеги.
 *
 * @param status Статус телеги ([BucketStatus]).
 * @param modifier Модификатор.
 */
@Composable
fun BucketStatusIcon(
    status: BucketStatus?,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        BucketStatus.WAITING_FOR_LOADING -> Icons.Default.Schedule to Color(0xFFFF9800)
        BucketStatus.MOVING -> Icons.Default.KeyboardDoubleArrowRight to Color(0xFF2196F3)
        BucketStatus.STOPPED_AT_POINT -> Icons.Default.PauseCircle to Color(0xFF9E9E9E)
        BucketStatus.UNLOADING -> Icons.Default.Downloading to Color(0xFF4CAF50)
        BucketStatus.DISCONNECTED -> Icons.Default.SignalWifiOff to Color(0xFFF44336)
        BucketStatus.UNDER_REPAIR -> Icons.Default.Construction to Color(0xFF795548)
        BucketStatus.MANUAL_CONTROL -> Icons.Default.Handyman to Color(0xFF9C27B0)
        BucketStatus.UNKNOWN, null -> Icons.AutoMirrored.Filled.Help to Color(0xFFBDBDBD)
    }

    Icon(
        imageVector = icon,
        contentDescription = status?.name ?: "Неизвестный статус",
        tint = color,
        modifier = modifier.size(24.dp)
    )
}