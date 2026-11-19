package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.domain.model.Load
import com.example.myapplication.presentation.util.DateTimeFormatters
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.viewmodel.LoadCardUiState

/**
 * Карточка заявки на отгрузку в модальном диалоге.
 *
 * Открывается по клику на номер заявки на экране бетоновозных телег.
 * Содержимое зависит от состояния загрузки:
 * - [LoadCardUiState.Loading] — индикатор загрузки;
 * - [LoadCardUiState.Error] — сообщение об ошибке с кнопкой «Повторить»;
 * - [LoadCardUiState.Success] — детальная информация о заявке;
 * - [LoadCardUiState.Hidden] — диалог не отображается.
 *
 * Закрытие доступно в любой момент: незавершённая загрузка отменяется
 * во ViewModel через [onDismiss].
 *
 * @param state Состояние карточки заявки из ViewModel.
 * @param onRetry Колбэк повторной загрузки с кодом заявки.
 * @param onDismiss Колбэк закрытия карточки.
 */
@Composable
fun LoadCardDialog(
    state: LoadCardUiState,
    onRetry: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val loadId = when (state) {
        is LoadCardUiState.Hidden -> return
        is LoadCardUiState.Loading -> state.loadId
        is LoadCardUiState.Success -> state.load.id
        is LoadCardUiState.Error -> state.loadId
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Заявка №$loadId") },
        text = {
            when (state) {
                is LoadCardUiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.Lg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is LoadCardUiState.Error -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(IconSize.Xs)
                    )
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is LoadCardUiState.Success -> LoadCardContent(load = state.load)

                is LoadCardUiState.Hidden -> Unit
            }
        },
        confirmButton = {
            if (state is LoadCardUiState.Error) {
                Button(onClick = { onRetry(loadId) }) { Text("Повторить") }
            } else {
                TextButton(onClick = onDismiss) { Text("Закрыть") }
            }
        },
        dismissButton = {
            if (state is LoadCardUiState.Error) {
                TextButton(onClick = onDismiss) { Text("Закрыть") }
            }
        }
    )
}

/**
 * Детальное содержимое карточки заявки: статус, продукция, участники
 * и временные отметки. Отсутствующие поля отображаются прочерком.
 *
 * Прокрутка длинного содержимого обеспечивается самим [AlertDialog].
 *
 * @param load Модель заявки на отгрузку.
 */
@Composable
private fun LoadCardContent(load: Load) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Smd)) {
        // Статус заявки: иконка переиспользуется из QueueScreen (тот же пакет)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)
        ) {
            StatusIcon(status = load.status)
            Text(
                text = load.statusName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider()

        // 1) Продукция и отгрузка
        CardInfoRow(label = "Продукт", value = load.product?.name ?: "—")
        CardInfoRow(
            label = "Количество",
            value = buildString {
                append(load.quality)
                load.terminal?.measure?.let { append(" $it") }
            }
        )
        CardInfoRow(label = "Терминал", value = load.terminal?.name ?: "—")
        CardInfoRow(label = "Позиция в очереди", value = load.queueNum?.toString() ?: "—")
        CardInfoRow(label = "Номер накладной", value = load.documentNum ?: "—")

        HorizontalDivider()

        // 2) Участники и транспорт
        CardInfoRow(label = "Транспорт", value = load.transport?.number ?: "—")
        CardInfoRow(label = "Подразделение", value = load.department?.name ?: "—")
        CardInfoRow(label = "Создал", value = load.creator?.fullName ?: "—")
        CardInfoRow(label = "Отгрузил", value = load.operator?.fullName ?: "—")

        HorizontalDivider()

        // 3) Временные отметки
        CardInfoRow(
            label = "Дата отгрузки",
            value = load.date?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
        CardInfoRow(
            label = "Прибытие на площадку",
            value = load.arrivalDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
        CardInfoRow(
            label = "Убытие",
            value = load.departDate?.format(DateTimeFormatters.dateTimeFormatter) ?: "—"
        )
    }
}

/**
 * Строка "метка-значение" в карточке заявки.
 *
 * @param label Название поля.
 * @param value Значение поля.
 */
@Composable
private fun CardInfoRow(label: String, value: String) {
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
        Spacer(modifier = Modifier.width(Spacing.Sm))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}