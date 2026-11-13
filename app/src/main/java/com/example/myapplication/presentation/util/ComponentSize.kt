package com.example.myapplication.presentation.util

import androidx.compose.ui.unit.dp

/**
 * Размеры типовых компонентов UI: кнопки, поля, строки списков, индикаторы загрузки.
 */
object ComponentSize {
    /** Высота основной кнопки (AuthScreen, ArchiveScreen, SettingsScreen). */
    val ButtonHeight = 48.dp

    /** Высота компактной кнопки (пагинация архива). */
    val SmallButtonHeight = 40.dp

    /** Высота поля выбора даты/времени (PickerFieldButton). */
    val PickerFieldHeight = 44.dp

    /** Минимальная высота строки списка (ProductListScreen). */
    val ListRowMinHeight = 56.dp

    /** Минимальная зона нажатия IconButton по Material-гайдлайнам (Spacer в MainTopBar). */
    val IconButtonTouchTarget = 48.dp

    /** Маленький индикатор загрузки (статус подключения в SettingsScreen). */
    val ProgressSm = 16.dp

    /** Средний индикатор загрузки (пагинация архива). */
    val ProgressMd = 24.dp

    /** Крупный индикатор загрузки (контент архива). */
    val ProgressLg = 32.dp

    /** Очень крупный индикатор загрузки (оверлеи QueueScreen/ArchiveScreen). */
    val ProgressXl = 48.dp

    /** Тонкая линия индикатора загрузки. */
    val ProgressStrokeThin = 2.dp

    /** Толстая линия индикатора загрузки (оверлей QueueScreen). */
    val ProgressStrokeThick = 4.dp

    /** Ширина выпадающего поля количества записей (ArchiveScreen). */
    val RecordsFieldWidth = 90.dp

    /** Толщина рамки поля выбора даты (PickerFieldButton). */
    val BorderWidth = 1.dp
}