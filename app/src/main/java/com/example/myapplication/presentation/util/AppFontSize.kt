package com.example.myapplication.presentation.util

import androidx.compose.ui.unit.sp

/**
 * Компактная шкала размеров текста для плотных интерфейсов
 * (таблица архива, подписи навигационного rail, поля фильтров).
 *
 * Для основного текста следует использовать MaterialTheme.typography.
 */
object AppFontSize {
    /** Подписи: ячейки таблицы, лейблы NavigationRail, метки полей. */
    val Caption = 12.sp

    /** Основной компактный текст: кнопки пагинации, сообщения, значение пикера. */
    val Body = 13.sp

    /** Текст полей ввода и пунктов выпадающих списков (ArchiveScreen). */
    val Field = 14.sp
}