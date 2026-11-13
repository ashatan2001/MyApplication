package com.example.myapplication.presentation.util

import androidx.compose.ui.unit.dp

/**
 * Уровни теней (elevation) приложения.
 *
 * Единая шкала высот для карточек, панелей оболочки и диалогов,
 * чтобы тени были консистентными во всех экранах.
 */
object AppElevation {
    /** Без тени. */
    val None = 0.dp

    /** Тень карточек списков (очередь заявок, бетоновозные телеги). */
    val Card = 2.dp

    /** Тень панелей оболочки MainShell: TopAppBar, NavigationBar, NavigationRail. */
    val Bar = 4.dp

    /** Тень всплывающих поверхностей: диалоги, выпадающие меню. */
    val Dialog = 8.dp
}