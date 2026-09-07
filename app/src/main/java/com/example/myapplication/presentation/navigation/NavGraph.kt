package com.example.myapplication.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.AuthState
import com.example.domain.usecase.ObserveAuthStateUseCase
import com.example.myapplication.presentation.ui.AuthScreen
import com.example.myapplication.presentation.ui.HomeScreen
import com.example.myapplication.presentation.ui.UserScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object UserInfo : Screen("user_info/{userId}") {
        fun createRoute(userId: Int) = "user_info/$userId"
    }
    data object UserAuth : Screen("user_auth")
}

@Composable
fun NavGraph(observeAuthStateUseCase: ObserveAuthStateUseCase) {
    val navController = rememberNavController()
    val authState by observeAuthStateUseCase().collectAsStateWithLifecycle(initialValue = AuthState.Loading)

    // ИСПРАВЛЕНО: Стартовый экран всегда Splash
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // Splash-экран с индикатором загрузки
        composable(Screen.Splash.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            // ИСПРАВЛЕНО: Навигация происходит только после определения состояния
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Authenticated -> {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        navController.navigate(Screen.UserAuth.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    else -> {} // Остается на Splash во время Loading
                }
            }
        }

        // Экран авторизации
        composable(Screen.UserAuth.route) {
            AuthScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.UserAuth.route) { inclusive = true }
                }
            })
        }

        // Главный экран
        composable(Screen.Home.route) {
            HomeScreen(
                onOpenUser = { userId ->
                    navController.navigate(Screen.UserInfo.createRoute(userId))
                },
                onLogout = {
                    navController.navigate(Screen.UserAuth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Экран информации о пользователе
        composable(
            route = Screen.UserInfo.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserScreen(userId = userId)
        }
    }
}