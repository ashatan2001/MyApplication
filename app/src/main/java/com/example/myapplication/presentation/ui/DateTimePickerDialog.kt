package com.example.myapplication.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.util.DateTimeFormatters.LOCALE
import com.example.myapplication.presentation.util.DateTimeFormatters.PATTERN_DATE
import com.example.myapplication.presentation.util.DateTimeFormatters.PATTERN_TIME
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeDateTimePickerDialog(
    initialDateMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onDateTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(initialDateMillis) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    if (!showTimePicker) {
        // Шаг 1: Выбор даты
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showTimePicker = true
                }) {
                    Text("Далее")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Выберите дату") }
            )
        }
    } else {
        // Шаг 2: Выбор времени
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Выберите время") },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val date = Date(selectedDateMillis)
                    val dateFormatter = SimpleDateFormat(PATTERN_DATE, LOCALE)
                    val dateStr = dateFormatter.format(date)

                    val timeStr = String.format(LOCALE, PATTERN_TIME, timePickerState.hour, timePickerState.minute)

                    onDateTimeSelected("$dateStr $timeStr")
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun PickerFieldButton(
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier
            .height(44.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape = shape)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, fontSize = 13.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
