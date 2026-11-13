package com.example.myapplication.presentation.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Формы скругления углов приложения.
 *
 * Типы объявлены явно ([RoundedCornerShape]), чтобы:
 * - исключить рекурсивный вывод типов компилятором при использовании
 *   констант внутри Composable-вызовов;
 * - сохранить совместимость с компонентами, принимающими именно
 *   [RoundedCornerShape] (например, PickerFieldButton).
 */
object AppShape {
    /** Чипы, бейджи. */
    val Small: RoundedCornerShape = RoundedCornerShape(4.dp)

    /** Поля, кнопки, карточки. */
    val Medium: RoundedCornerShape = RoundedCornerShape(8.dp)

    /** Промежуточная форма (M3 medium): поле поиска SearchField. */
    val MediumLarge: RoundedCornerShape = RoundedCornerShape(12.dp)

    /** Модалки, диалоги. */
    val Large: RoundedCornerShape = RoundedCornerShape(16.dp)

    /** FAB, большие карточки. */
    val ExtraLarge: RoundedCornerShape = RoundedCornerShape(28.dp)
}