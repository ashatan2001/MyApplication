package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.domain.model.Product
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing

/**
 * Диалоговое окно создания заявки на отгрузку продукта.
 *
 * Отображает продукт и название точки выгрузки (не сырой ID), позволяет
 * указать количество и номер паллеты.
 *
 * Количество хранится как сырой текст: поле можно очистить и посимвольно
 * ввести дробное значение с любым разделителем; подтверждение блокируется
 * при невалидном значении (не число или <= 0).
 *
 * Во время отправки поля и кнопки блокируются, в кнопке подтверждения
 * показывается индикатор (паттерн M3 «прогресс внутри кнопки»). Ошибка
 * создания показывается прямо в диалоге — введённые данные не теряются,
 * отправку можно повторить. Закрытие во время отправки заблокировано,
 * чтобы результат операции не остался без адресата.
 *
 * @param product Модель продукта для отгрузки.
 * @param zoneName Название точки выгрузки для отображения.
 * @param isSubmitting Флаг выполнения запроса на создание заявки.
 * @param errorMessage Ошибка создания заявки для показа в диалоге; null — ошибки нет.
 * @param onDismiss Колбэк закрытия диалога без сохранения.
 * @param onSubmit Колбэк отправки заявки с количеством и номером паллеты.
 */
@Composable
fun CreateTaskDialog(
    product: Product,
    zoneName: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (quantity: Float, pallete: String?) -> Unit
) {
    // Стартовое количество: значение по умолчанию продукта без хвостового ".0"
    val defaultQuantity = product.quantity ?: 1f
    var quantityText by rememberSaveable(product.id) {
        mutableStateOf(
            if (defaultQuantity % 1f == 0f) defaultQuantity.toInt().toString()
            else defaultQuantity.toString()
        )
    }
    var pallete by rememberSaveable(product.id) { mutableStateOf("") }

    val quantity = quantityText.replace(',', '.').toFloatOrNull()
    val isValid = quantity != null && quantity > 0f
    val isError = quantityText.isNotBlank() && !isValid
    val canSubmit = isValid && !isSubmitting

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        modifier = Modifier.imePadding(),
        title = {
            Text(
                text = "Заявка на отгрузку",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Smd)) {
                Text(
                    text = "Продукт: ${product.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Точка выгрузки: $zoneName",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        // Разрешаем только цифры и не более одного разделителя
                        if (input.count { it == '.' || it == ',' } <= 1 &&
                            input.all { it.isDigit() || it == '.' || it == ',' }
                        ) {
                            quantityText = input
                        }
                    },
                    label = { Text("Количество") },
                    isError = isError,
                    supportingText = { if (isError) Text("Введите число больше нуля") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pallete,
                    onValueChange = { pallete = it },
                    label = { Text("Номер паллеты") },
                    supportingText = {
                        Text(
                            "Необязательное поле",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSubmit) quantity?.let { onSubmit(it, pallete.ifBlank { null }) }
                        }
                    ),
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )

                // Ошибка создания заявки: рядом с источником, озвучивается TalkBack
                if (errorMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(IconSize.Xs)
                        )
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { quantity?.let { onSubmit(it, pallete.ifBlank { null }) } },
                enabled = canSubmit
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(IconSize.Sm),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = ComponentSize.ProgressStrokeThin
                    )
                    Spacer(Modifier.width(Spacing.Sm))
                    Text("Отправка…")
                } else {
                    Text("Отправить")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Отмена")
            }
        }
    )
}