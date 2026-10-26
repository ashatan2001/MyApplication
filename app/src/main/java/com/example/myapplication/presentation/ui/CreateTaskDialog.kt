package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.Product

/**
 * Диалоговое окно создания заявки на отгрузку продукта.
 * Отображает информацию о продукте и точке выгрузки, позволяет указать количество и номер паллеты.
 *
 * @param product Модель продукта для отгрузки.
 * @param zone ID точки выгрузки.
 * @param onDismiss Колбэк закрытия диалога без сохранения.
 * @param onSubmit Колбэк отправки заявки с переданным количеством и номером паллеты.
 */
@Composable
fun CreateTaskDialog(
    product: Product,
    zone: Int,
    onDismiss: () -> Unit,
    onSubmit: (quantity: Float, pallete: String?) -> Unit
) {
    var quantity by remember { mutableStateOf(product.quantity ?: 1f) }
    var pallete by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Заявка на отгрузку",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Информация о продукте
                Text(
                    text = "Продукт: ${product.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Информация о точке выгрузки
                Text(
                    text = "Точка выгрузки: $zone",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Поле ввода количества
                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { it.toFloatOrNull()?.let { q -> quantity = q } },
                    label = { Text("Количество") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Поле ввода номера паллеты
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(quantity, pallete.ifBlank { null })
            }) {
                Text("Отправить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}