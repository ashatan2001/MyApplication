package com.example.myapplication.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.domain.model.AuthState
import com.example.myapplication.presentation.ui.AuthScreen
import com.example.myapplication.presentation.ui.HomeScreen
import com.example.myapplication.presentation.ui.UserScreen
import com.example.myapplication.presentation.viewmodel.SessionViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // Глобальный слушатель событий логаута из SessionViewModel (если используется)
    LaunchedEffect(Unit) {
        viewModel.logoutEvents.collect {
            onLogout() // Вызываем колбэк из MainActivity для полной очистки стека
        }
    }

    // Автоматическая навигация при изменении состояния авторизации
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate(Routers.Home.route) {
                    popUpTo(Routers.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(Routers.UserAuth.route) {
                    popUpTo(Routers.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> {} // Остается на Splash во время Loading
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routers.Splash.route
    ) {
        composable(Routers.Splash.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable(Routers.UserAuth.route) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Routers.Home.route) {
                        popUpTo(Routers.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routers.Home.route) {
            HomeScreen(
                onOpenUser = {
                    navController.navigate(Routers.UserInfo.route)
                },
                onLogout = onLogout // Пробрасываем колбэк полной очистки стека из MainActivity
            )
        }

        composable(Routers.UserInfo.route) {
            UserScreen(
                onBack = {
                    navController.popBackStack() // Простой возврат назад, так как Home есть в стеке
                },
                viewModel = hiltViewModel()
            )
        }
    }
}