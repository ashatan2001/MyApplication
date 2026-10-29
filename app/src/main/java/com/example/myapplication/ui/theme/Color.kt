package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

// Основные (брендовые) голубые оттенки
val BluePrimary   = Color(0xFF258BC8) // основной, для светлой темы
val BluePrimaryDark = Color(0xFF64B5F6) // светлее, для контраста на тёмном фоне
val BlueContainer = Color(0xFFE1F5FE) // светлая подложка для primary в light-теме

// Нейтральные серые (используются вместо избыточных secondary/tertiary)
val Grey50  = Color(0xFFFAFAFA)
val Grey100 = Color(0xFFF5F5F5)
val Grey300 = Color(0xFFE0E0E0)
val Grey400 = Color(0xFFBDBDBD)
val Grey600 = Color(0xFF757575)
val Grey700 = Color(0xFF616161)
val Grey800 = Color(0xFF424242)
val Grey850 = Color(0xFF1E1E1E)
val Grey900 = Color(0xFF121212)

// Контрастные цвета для иконок/текста на primary
val OnPrimaryLight = Color(0xFFFFFFFF) // белый на голубом в светлой теме
val OnPrimaryDark  = Color(0xFF003249) // тёмно-синий на голубом в тёмной теме

// Цвета ошибок (Material 3 стандартные) — отдельная группа,
// поскольку не должна пересекаться с нейтральными серыми.
val ErrorLight              = Color(0xFFB00020)
val ErrorDark               = Color(0xFFCF6679)
val OnErrorLight            = Color(0xFFFFFFFF)
val OnErrorDark             = Color(0xFF000000)
val ErrorContainerLight     = Color(0xFFFDECEA)
val ErrorContainerDark      = Color(0xFF960019)
val OnErrorContainerLight   = Color(0xFF370617)
val OnErrorContainerDark    = Color(0xFFFFDAD4)