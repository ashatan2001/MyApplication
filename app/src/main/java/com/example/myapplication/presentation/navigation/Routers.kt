package com.example.myapplication.presentation.navigation

/**
 * Централизованное управление маршрутами приложения.
 * Использование [sealed] гарантирует исчерпывающий `when` и защиту
 * от опечаток в строковых литералах маршрутов.
 */
sealed class Routers(val route: String) {
    /** Стартовый экран. Определяет, авторизован ли пользователь. */
    data object Splash : Routers("splash")

    /** Экран входа (логин/пароль). */
    data object UserAuth : Routers("user_auth")

    /** Главный экран с приветствием и боковым меню. */
    data object Home : Routers("home")

    /** Экран профиля пользователя. */
    data object PersonInfo : Routers("person_info")

    /** Экран списка точек выгрузки бетона. */
    data object DstPointsList : Routers("dst_points_list")

    /** Экран точки выгрузки бетона. */
    data object DstPoint : Routers("dst_point/{zoneId}") {
        const val ARG_ZONE_ID = "zoneId"

        fun createRoute(zoneId: Int): String {
            return "dst_point/$zoneId"
        }
    }

    /** Экран просмотра архивов с выпадающим списком типов. */
    data object Archive : Routers("archive")
}