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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.domain.model.AuthState
import com.example.myapplication.presentation.ui.*
import com.example.myapplication.presentation.viewmodel.SessionViewModel

/**
 * Корневой граф навигации приложения.
 * Автоматически перенаправляет пользователя в зависимости от [AuthState]:
 * [AuthState.Authenticated] → [Routers.Home]
 * [AuthState.Unauthenticated] → [Routers.UserAuth]
 *
 * @param navController Контроллер навигации из [androidx.navigation.compose.rememberNavController].
 * @param onLogout Колбэк для выхода (вызывается из бокового меню HomeScreen).
 * @param viewModel [SessionViewModel] для наблюдения за состоянием сессии.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigateClearingBackStack("main_shell")
            }
            is AuthState.Unauthenticated -> {
                navController.navigateClearingBackStack(Routers.UserAuth.route)
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routers.Splash.route
    ) {
        composable(Routers.Splash.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable(Routers.UserAuth.route) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigateClearingBackStack("main_shell")
                }
            )
        }

        composable("main_shell") {
            MainShell(onLogout = onLogout)
        }
    }
}