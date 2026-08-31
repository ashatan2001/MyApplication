package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.presentation.home.HomeScreen
import com.example.app.presentation.user.UserScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object UserInfo : Screen("user_info/{userId}") {
        fun createRoute(userId: Int) = "user_info/$userId"
    }

    object UserAuth : Screen("user_auth") {
        fun createRoute(userId: Int) = "user_auth"
    }
}

@Composable
fun NavGraph() {
    // Создаем контроллер навигации
    val navController = rememberNavController()

    // Настраиваем навигационный граф
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        // Главный экран
        composable(route = Screen.Home.route) {
            HomeScreen(
                onOpenUser = { userId ->
                    navController.navigate(Screen.UserInfo.createRoute(userId))
                },
            )
        }

        // Информация пользователя
        composable(
            route = Screen.UserInfo.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            UserScreen(userId = userId)
        }
    }
}