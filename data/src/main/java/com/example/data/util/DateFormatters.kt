package com.example.data.util

import java.time.format.DateTimeFormatter

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