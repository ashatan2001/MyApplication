package com.example.myapplication.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.presentation.util.AppAlpha
import com.example.myapplication.presentation.util.AppFontSize
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.DateTimeFormatters.LOCALE
import com.example.myapplication.presentation.util.DateTimeFormatters.PATTERN_DATE
import com.example.myapplication.presentation.util.DateTimeFormatters.PATTERN_TIME
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import java.text.SimpleDateFormat
import java.util.*

/**
 * Двухшаговый диалог выбора даты и времени.
 *
 * Шаг 1 — выбор даты ([DatePicker]), шаг 2 — выбор времени ([TimePicker]).
 * Результат передаётся строкой в формате "dd.MM.yyyy HH:mm:ss" (секунды всегда 00).
 *
 * @param initialDateMillis Начальная дата в миллисекундах (по умолчанию — текущий момент).
 * @param onDismiss Колбэк закрытия диалога без выбора.
 * @param onDateTimeSelected Колбэк с отформатированной строкой даты и времени.
 */
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
            DatePicker(state = datePickerState)
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
                    // Обновляем календарь выбранным временем
                    calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    calendar.set(Calendar.MINUTE, timePickerState.minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    selectedDateMillis = calendar.timeInMillis

                    // Форматируем дату и время
                    val date = Date(selectedDateMillis)
                    val dateFormatter = SimpleDateFormat(PATTERN_DATE, LOCALE)
                    val dateStr = dateFormatter.format(date)

                    // Формат времени с секундами для сервера (секунды всегда 00).
                    // Используем общий паттерн вместо дубликата строки "HH:mm:ss".
                    val timeFormatter = SimpleDateFormat(PATTERN_TIME, LOCALE)
                    val timeStr = timeFormatter.format(date)

                    // Передаем полный формат "dd.MM.yyyy HH:mm:ss"
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

/**
 * Кнопка-поле для открытия диалога выбора даты/времени.
 * Отображает текущее значение с иконкой на полупрозрачной подложке с рамкой.
 *
 * @param value Отображаемое значение (дата или плейсхолдер).
 * @param icon Иконка слева от значения.
 * @param onClick Колбэк нажатия (открытие диалога).
 * @param shape Форма скругления углов (например, [com.example.myapplication.presentation.util.AppShape.Medium]).
 * @param modifier Модификатор контейнера.
 */
@Composable
fun PickerFieldButton(
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.SubtleContainer),
        modifier = modifier
            .height(ComponentSize.PickerFieldHeight)
            .border(
                width = ComponentSize.BorderWidth,
                color = MaterialTheme.colorScheme.outline.copy(alpha = AppAlpha.SubtleBorder),
                shape = shape
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(IconSize.Sm),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.Sm))
            Text(
                text = value,
                fontSize = AppFontSize.Body,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}