package com.example.myapplication.presentation.util

/**
 * Уровни прозрачности для оверлеев, подложек и рамок.
 */
object AppAlpha {
    /** Плотный оверлей блокировки экрана при сетевой операции (QueueScreen). */
    const val ScrimStrong = 0.85f

    /** Оверлей загрузки страницы (ArchiveScreen). */
    const val Scrim = 0.6f

    /** Полупрозрачная подложка поля выбора даты (PickerFieldButton). */
    const val SubtleContainer = 0.3f

    /** Полупрозрачная рамка поля выбора даты (PickerFieldButton). */
    const val SubtleBorder = 0.5f
}