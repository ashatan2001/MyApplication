package com.example.myapplication.presentation.navigation

/**
 * Централизованное управление маршрутами приложения.
 */
sealed class Routers(val route: String) {
    data object Splash : Routers("splash")
    data object UserAuth : Routers("user_auth")
    data object Home : Routers("home")
    data object UserInfo : Routers("user_info")
}