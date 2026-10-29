package com.example.myapplication.presentation.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object AppShape {
    val Small = RoundedCornerShape(4.dp)   // чипы, бейджи
    val Medium = RoundedCornerShape(8.dp)  // поля, кнопки, карточки
    val Large = RoundedCornerShape(16.dp)  // модалки, диалоги
    val ExtraLarge = RoundedCornerShape(28.dp) // FAB, большие карточки
}