package com.example.myapplication.presentation.util

import androidx.compose.ui.graphics.Color

/**
 * Семантическая палитра статусов заявок и бетоновозных телег.
 *
 * Устраняет дублирование hex-значений между QueueScreen и BucketScreen.
 * Значения соответствуют Material-палитре (Blue 500, Green 500, Red 500 и т.д.).
 *
 * В перспективе цвета стоит перенести в тему (light/dark), см. рекомендации.
 */
object StatusColors {
    /** Синий: в очереди, в пути. */
    val Info = Color(0xFF2196F3)

    /** Зелёный: выполнено, разгрузка, телега загружена. */
    val Success = Color(0xFF4CAF50)

    /** Красный: брак, потеря связи. */
    val Error = Color(0xFFF44336)

    /** Янтарный: взвешена, ожидает загрузки. */
    val Warning = Color(0xFFFF9800)

    /** Серый: отменена, остановлена, телега пуста. */
    val Neutral = Color(0xFF9E9E9E)

    /** Коричневый: неизвестный статус заявки, ремонт телеги. */
    val Maintenance = Color(0xFF795548)

    /** Фиолетовый: ручное управление телегой. */
    val Manual = Color(0xFF9C27B0)

    /** Светло-серый: статус телеги неизвестен/отсутствует. */
    val Unknown = Color(0xFFBDBDBD)
}