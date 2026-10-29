package com.example.myapplication.presentation.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Задаёт тон иконок статус-бара для текущего экрана.
 *
 * [SideEffect] повторно выставляет флаг окна после каждой успешной перекомпоновки,
 * поэтому при смене маршрута тон обновляется автоматически.
 *
 * @param lightIcons true — светлые иконки (под статус-баром тёмный фон, например TopAppBar);
 *                   false — тёмные иконки (светлый фон, например AuthScreen).
 */
@Composable
fun StatusBarIconAppearance(lightIcons: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightStatusBars = !lightIcons
    }
}