package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.presentation.auth.AuthScreen
import com.example.app.presentation.home.HomeScreen
import com.example.app.presentation.user.UserScreen
import com.example.domain.model.AuthState
import com.example.domain.usecase.CheckAuthStateUseCase


sealed class Screen(val route: String) {
    object Home : Screen("home")

    object UserInfo : Screen("user_info/{userId}") {
        fun createRoute(userId: Int) = "user_info/$userId"
    }

    object UserAuth : Screen("user_auth") // Упрощено, userId здесь не нужен
}

@Composable
fun NavGraph(
    checkAuthStateUseCase: CheckAuthStateUseCase // <-- Передаем UseCase для проверки состояния
) {
    val navController = rememberNavController()

    // 1. Наблюдаем за глобальным состоянием авторизации
    val authState by checkAuthStateUseCase().collectAsState(initial = AuthState.Unauthenticated)

    // 2. Динамически выбираем стартовый экран
    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Home.route
        is AuthState.Unauthenticated -> Screen.UserAuth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // 3. Экран авторизации
        composable(route = Screen.UserAuth.route) {
            AuthScreen(
                onLoginSuccess = {
                    // При успешном входе переходим на Home и очищаем стек назад
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UserAuth.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 4. Главный экран
        composable(route = Screen.Home.route) {
            HomeScreen(
                onOpenUser = { userId ->
                    navController.navigate(Screen.UserInfo.createRoute(userId))
                },
                onLogout = { // <-- Добавлен колбэк для выхода
                    navController.navigate(Screen.UserAuth.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 5. Информация пользователя
        composable(
            route = Screen.UserInfo.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            UserScreen(userId = userId)
        }
    }
}