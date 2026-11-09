package com.example.data.util

import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Централизованное хранилище форматтеров дат для всего data-слоя.
 * Сервер использует единый формат "dd.MM.yyyy HH:mm:ss" во всех ответах
 * (события, периоды архива и т.д.).
 * Вынесено в отдельный объект, чтобы:
 * - избежать дублирования в разных мапперах;
 * - переиспользовать скомпилированный [java.time.format.DateTimeFormatter] (его создание дорогое);
 * - менять формат в одном месте при изменении контракта с сервером.
 */
object DateFormatters {
    /** Формат даты, возвращаемый сервером (например: "05.10.2026 14:30:00"). */
    val SERVER_DATE_TIME: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    /** Формат даты для отображения в UI (дд.мм.гггг чч:мм:сс). */
    val DISPLAY_DATE_TIME: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
}

/**
 * Безопасно парсит строку даты в [LocalDateTime].
 * Пытается распарсить строку сначала в ISO-формате, затем в формате сервера.
 *
 * @param dateString Строка с датой для парсинга.
 * @return Распарсенная дата или null, если строка пуста, равна null или не соответствует
 *         ожидаемым форматам.
 */
fun parseDate(dateString: String?): LocalDateTime? {
    if (dateString.isNullOrBlank()) {
        return null
    }

    return try {
        val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime.parse(dateString, isoFormatter)
    } catch (e: DateTimeParseException) {
        try {
            LocalDateTime.parse(dateString, DateFormatters.SERVER_DATE_TIME)
        } catch (e2: DateTimeParseException) {
            Timber.e(e2, "Ошибка парсинга даты: '$dateString'")
            null
        }
    }
}