package com.example.myapplication.presentation.navigation

import androidx.navigation.NavController

/**
 * Навигация с полной очисткой стека.
 *
 * Используется при переходах, после которых возврат назад невозможен
 * (например, после успешного входа или принудительного выхода из системы).
 *
 * @param route Целевой маршрут из [Routers].
 */
fun NavController.navigateClearingBackStack(route: String) {
    navigate(route) {
        // Удаляем весь стек до Splash включительно,
        // чтобы кнопка "Назад" не возвращала на предыдущие экраны
        popUpTo(Routers.Splash.route) { inclusive = true }
        // Не создаём дубликат, если целевой экран уже на вершине стека
        launchSingleTop = true
    }
}