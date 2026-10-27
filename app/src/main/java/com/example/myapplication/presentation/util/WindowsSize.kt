package com.example.myapplication.presentation.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Классы размера окна приложения (аналог WindowSizeClass из Material 3).
 *
 * Границы соответствуют рекомендациям Material Design:
 * - [COMPACT] — телефоны (< 600dp);
 * - [MEDIUM] — планшеты в портретной ориентации (600–840dp);
 * - [EXPANDED] — планшеты в ландшафтной ориентации и крупные экраны (> 840dp).
 */
enum class WindowSize { COMPACT, MEDIUM, EXPANDED }

/**
 * Определяет текущий класс размера окна по ширине экрана из [LocalConfiguration].
 * Автоматически пересчитывается при повороте устройства и изменении размера окна.
 *
 * @return Текущий [WindowSize].
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 600 -> WindowSize.COMPACT
        configuration.screenWidthDp < 840 -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}