package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Светлая цветовая схема.
 *
 * Все цвета взяты из именованных констант [Color.kt].
 * Вторичные и третичные тона намеренно используют нейтральные серые
 * ([Grey800], [Grey100]) — это сохраняет минимализм палитры.
 */
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = OnPrimaryLight,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BluePrimary,

    secondary = Grey800,
    onSecondary = OnPrimaryLight,
    secondaryContainer = Grey100,
    onSecondaryContainer = Grey800,

    tertiary = Grey800,
    onTertiary = OnPrimaryLight,
    tertiaryContainer = Grey100,
    onTertiaryContainer = Grey800,

    background = Grey50,
    onBackground = Grey800,
    surface = Grey100,
    onSurface = Grey800,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey600,

    outline = Grey400,
    outlineVariant = Grey300,

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

/**
 * Тёмная цветовая схема.
 *
 * Использует светло-голубой [BluePrimaryDark] для сохранения контраста
 * и глубокие тёмно-серые тона для фона, как требует Material 3.
 */
private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = BluePrimary,
    onPrimaryContainer = OnPrimaryLight,

    secondary = Grey300,
    onSecondary = OnPrimaryDark,
    secondaryContainer = Grey850,
    onSecondaryContainer = Grey300,

    tertiary = Grey300,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = Grey850,
    onTertiaryContainer = Grey300,

    background = Grey900,
    onBackground = Grey300,
    surface = Grey850,
    onSurface = Grey300,
    surfaceVariant = Grey850,
    onSurfaceVariant = Grey300,

    outline = Grey700,
    outlineVariant = Grey800,

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

/**
 * Тема приложения.
 *
 * При `dynamicColor = true` и Android 12+ применяются системные Material You цвета.
 * Иначе — собственная схема ([LightColorScheme] или [DarkColorScheme]),
 * сохраняющая брендовый голубой цвет во всех ключевых компонентах.
 *
 * @param darkTheme Использовать тёмную схему (по умолчанию — системная настройка).
 * @param dynamicColor Включить Material You (по умолчанию выключено).
 * @param content Содержимое, отрисовываемое с применённой темой.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}