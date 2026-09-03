package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Home.route
        else -> Screen.UserAuth.route
    }

    // Принудительная навигация при смене состояния авторизации
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                navController.navigate(Screen.UserAuth.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route == Screen.UserAuth.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UserAuth.route) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.UserAuth.route) {
            AuthScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.UserAuth.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onOpenUser = { userId -> navController.navigate(Screen.UserInfo.createRoute(userId)) },
                onLogout = {
                    navController.navigate(Screen.UserAuth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.UserInfo.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserScreen(userId = userId)
        }
    }
}