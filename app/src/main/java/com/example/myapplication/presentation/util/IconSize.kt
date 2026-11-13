package com.example.myapplication.presentation.util

import androidx.compose.ui.unit.dp

/**
 * Стандартные размеры иконок приложения.
 *
 * Использование единой шкалы вместо магических значений `Modifier.size(...)`
 * упрощает визуальную настройку всего UI из одного места.
 */
object IconSize {
    /** Очень маленькая: индикаторы статуса подключения (SettingsScreen). */
    val Xs = 16.dp

    /** Маленькая: иконки внутри кнопок (AuthScreen, SettingsScreen, PickerFieldButton). */
    val Sm = 18.dp

    /** Инлайн-иконки в текстовых кнопках (DstPointListScreen). */
    val Inline = 20.dp

    /** Стандартная: иконки статусов, шевроны раскрытия (QueueScreen, BucketScreen). */
    val Md = 24.dp

    /** Крупная: иконка заказа продукта (ProductListScreen). */
    val Lg = 28.dp

    /** Компактная кнопка-действие (IconButton в ProductListScreen). */
    val Action = 40.dp

    /** Очень крупная: иконки ошибок и большие индикаторы загрузки. */
    val Xl = 48.dp

    /** Аватар пользователя (PersonScreen). */
    val Avatar = 100.dp
}