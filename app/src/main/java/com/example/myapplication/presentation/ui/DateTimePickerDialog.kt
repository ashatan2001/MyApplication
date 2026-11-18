package com.example.myapplication.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.presentation.util.AppAlpha
import com.example.myapplication.presentation.util.AppFontSize
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.DateTimeFormatters
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Двухшаговый диалог выбора даты и времени.
 *
 * Шаг 1 — выбор даты через [DatePicker], шаг 2 — ввод времени через [TimeInput].
 * Результат передаётся строкой в формате "dd.MM.yyyy HH:mm:ss" (секунды всегда 00).
 *
 * Все преобразования "millis ↔ дата" выполняются в UTC ([ZoneOffset.UTC]).
 * Это необходимо, потому что Material 3 [DatePicker] хранит выбранный день
 * как полночь UTC; использование локального часового пояса на любой из
 * границ (чтение или форматирование) приводило к сдвигу дня на устройствах
 * в часовых поясах западнее или восточнее UTC.
 *
 * Состояния пикеров объявлены через [rememberSaveable] и подняты над шагами,
 * поэтому поворот экрана или возврат со шага времени не сбрасывают выбор.
 *
 * @param initialDateMillis Начальная дата в UTC-миллисекундах
 *        (например, результат [DateTimeFormatters.parseToUtcMillis]);
 *        `null` означает «сегодня» по локальному календарю.
 * @param onDismiss Колбэк закрытия диалога без выбора.
 * @param onDateTimeSelected Колбэк с отформатированной строкой даты и времени.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeDateTimePickerDialog(
    initialDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onDateTimeSelected: (String) -> Unit
) {
    // Начальная дата-время: парсим UTC-миллис либо берём локальное "сегодня"
    // как календарный день (без часового пояса).
    val initialDateTime: LocalDateTime = initialDateMillis
        ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDateTime() }
        ?: LocalDate.now().atStartOfDay()

    // Сохраняемый между шагами выбранный день: хранится как epochDay,
    // чтобы не зависеть от часового пояса и переживать смерть процесса.
    var selectedEpochDay by rememberSaveable {
        mutableLongStateOf(initialDateTime.toLocalDate().toEpochDay())
    }
    // Сохраняемый флаг текущего шага.
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    // DatePickerState создаётся с UTC-миллис из выбранного дня:
    // именно так пикер корректно подсвечивает день в сетке календаря.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.ofEpochDay(selectedEpochDay)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    // TimePickerState создаётся один раз над шагами: выбранное время
    // сохраняется при возврате со шага 2 на шаг 1.
    val timePickerState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
        is24Hour = true
    )

    if (!showTimePicker) {
        // === Шаг 1: Выбор даты ===
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Читаем UTC-день строго в UTC, без локальной зоны.
                        selectedEpochDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
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
        // === Шаг 2: Ввод времени ===
        // Используем TimeInput вместо циферблатного TimePicker: он компактен
        // и умещается в AlertDialog на узких экранах и при увеличенном системном шрифте,
        // где циферблат обрезается по краям.
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Выберите время") },
            text = {
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // Собираем день и время напрямую, без Calendar/SimpleDateFormat.
                    // Часовой пояс не участвует: результат — строка "dd.MM.yyyy HH:mm:ss".
                    val resultDateTime = LocalDate.ofEpochDay(selectedEpochDay)
                        .atTime(timePickerState.hour, timePickerState.minute)
                    onDateTimeSelected(resultDateTime.format(DateTimeFormatters.dateTimeFormatter))
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                // "Назад" возвращает к шагу даты без потери выбранного времени.
                // Тап вне диалога — полноценная отмена (onDismissRequest выше).
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Назад")
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.Sm)
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