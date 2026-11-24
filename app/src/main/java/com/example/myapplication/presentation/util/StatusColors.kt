package com.example.myapplication.presentation.util

import androidx.compose.ui.graphics.Color

/**
 * Семантическая палитра статусов заявок и бетоновозных телег.
 */
object StatusColors {

    /** Синий (Blue 500): движение, активный процесс, в т.ч. загрузка и разгрузка. */
    val Blue = Color(0xFF2196F3)

    /** Зелёный (Green 500): успех, завершение, готовность, наличие смеси. */
    val Green = Color(0xFF4CAF50)

    /** Красный (Red 500): ошибка, критическая проблема, потеря связи. */
    val Red = Color(0xFFF44336)

    /** Оранжевый (Orange 500): ожидание, промежуточный этап, требующий внимания. */
    val Orange = Color(0xFFFF9800)

    /** Серый (Gray 500): нейтральное или неактивное состояние (остановлена, отменена, архив, пуста). */
    val Gray = Color(0xFF9E9E9E)

    /** Коричневый (Brown 500): внеплановое техническое состояние (ремонт). */
    val Brown = Color(0xFF795548)

    /** Фиолетовый (Purple 500): специфические режимы (ручное управление). */
    val Purple = Color(0xFF9C27B0)

    /** Светло-серый (Gray 400): отсутствие данных, неопределённость, состояние по умолчанию. */
    val LightGray = Color(0xFFBDBDBD)
}