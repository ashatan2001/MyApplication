package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.Product

@Composable
fun CreateTaskDialog(
    product: Product,
    zone: Int,
    onDismiss: () -> Unit,
    onSubmit: (quantity: Float, pallete: String?) -> Unit
) {
    var quantity by rememberSaveable { mutableStateOf(1f) }
    var pallete by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Заявка на отгрузку") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Продукт: ${product.name}", style = MaterialTheme.typography.bodyMedium)
                Text("Точка выгрузки: $zone", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { it.toFloatOrNull()?.let { q -> quantity = q } },
                    label = { Text("Количество") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pallete,
                    onValueChange = { pallete = it },
                    label = { Text("Номер паллеты (необязательно)") },
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