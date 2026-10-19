package com.example.data.util

import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Централизованное хранилище форматтеров дат для всего data-слоя.
 *
 * Сервер использует единый формат "dd.MM.yyyy HH:mm:ss" во всех ответах
 * (события, периоды архива и т.д.).
 *
 * Вынесено в отдельный объект, чтобы:
 * - избежать дублирования в разных мапперах;
 * - переиспользовать скомпилированный [java.time.format.DateTimeFormatter] (его создание дорогое);
 * - менять формат в одном месте при изменении контракта с сервером.
 */
object DateFormatters {
    /** Формат даты, возвращаемый сервером (например: "05.10.2026 14:30:00"). */
    val SERVER_DATE_TIME: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
}

/**
 * Безопасно парсит строку даты в [LocalDateTime], используя единый формат сервера.
 *
 * При ошибке парсинга логирует исключение через [Timber] и возвращает значение по умолчанию.
 *
 * @param fallback Значение, которое будет возвращено в случае ошибки парсинга
 *                 (по умолчанию [LocalDateTime.MIN]).
 * @return Распарсенная дата или [fallback].
 */
fun parseDate(raw: String): LocalDateTime = try {
    LocalDateTime.parse(raw.trim(), DateFormatters.SERVER_DATE_TIME)
} catch (e: DateTimeParseException) {
    Timber.e(e, "Ошибка парсинга даты: '$raw'")
    LocalDateTime.MIN
}