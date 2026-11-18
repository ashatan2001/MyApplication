package com.example.myapplication.presentation.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Утилитарный объект для форматирования и парсинга дат и времени.
 *
 * Все преобразования "строка ↔ millis" выполняются в UTC: сервер принимает/возвращает
 * строки в формате "dd.MM.yyyy HH:mm:ss" без часового пояса, и Material 3 [DatePicker]
 * хранит выбранный день как полночь UTC. Единый пояс на всём пути исключает
 * сдвиг дня на устройствах с любым часовым поясом.
 */
object DateTimeFormatters {
    const val PATTERN_DATE_TIME = "dd.MM.yyyy HH:mm:ss"
    const val PATTERN_DATE = "dd.MM.yyyy"
    const val PATTERN_TIME = "HH:mm:ss"
    val LOCALE: Locale = Locale.ROOT

    /**
     * Форматтер для [LocalDateTime] в формате "dd.MM.yyyy HH:mm:ss".
     * Предназначен для использования с методом `LocalDateTime.format(...)`.
     */
    val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(PATTERN_DATE_TIME, LOCALE)

    /**
     * Парсит строку "dd.MM.yyyy HH:mm:ss" в UTC-миллисекунды.
     *
     * Строка трактуется как "настенное" время без часового пояса, поэтому
     * парсинг выполняется в UTC — симметрично форматированию результата пикера.
     * Это гарантирует, что Material 3 [DatePicker] (который трактует
     * `initialSelectedDateMillis` как UTC) подсветит ровно тот день,
     * что записан в строке, независимо от часового пояса устройства.
     *
     * @param dateStr Строка с датой и временем в формате [PATTERN_DATE_TIME].
     * @return UTC-миллисекунды или `null`, если строка пустая или невалидная.
     *         Возврат `null` позволяет диалогу открыть пикер на «сегодня»,
     *         а не на произвольный день.
     */
    fun parseToUtcMillis(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return runCatching {
            LocalDateTime.parse(dateStr, dateTimeFormatter)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        }.getOrNull()
    }
}