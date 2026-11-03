package com.example.myapplication.presentation.util

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Утилитарный объект для форматирования и парсинга дат и времени.
 */
object DateTimeFormatters {
    const val PATTERN_DATE_TIME = "dd.MM.yyyy HH:mm:ss"
    const val PATTERN_DATE = "dd.MM.yyyy"
    const val PATTERN_TIME = "HH:mm:ss"
    val LOCALE: Locale = Locale.ROOT

    /**
     * Форматтер для java.time.LocalDateTime, использующий заданный паттерн и локаль.
     * Предназначен для использования с методом .format() у объектов даты и времени.
     */
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN_DATE_TIME, LOCALE)

    /**
     * Парсит строку даты в миллисекунды (для совместимости со старым кодом).
     *
     * @param dateStr Строка с датой и временем.
     * @return Время в миллисекундах или текущее время при ошибке парсинга.
     */
    fun parseToMillis(dateStr: String): Long {
        return try {
            val formatter = SimpleDateFormat(PATTERN_DATE_TIME, LOCALE)
            formatter.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}