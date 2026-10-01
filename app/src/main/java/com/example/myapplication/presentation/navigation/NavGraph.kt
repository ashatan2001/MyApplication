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
 *
 * Автоматически перенаправляет пользователя в зависимости от [AuthState]:
 * - [AuthState.Authenticated] → [Routers.Home]
 * - [AuthState.Unauthenticated] → [Routers.UserAuth]
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

    // Реактивная навигация при изменении состояния сессии.
    // Когда CustomCookieJar.clear() очищает сессию, поток эмитит
    // AuthState.Unauthenticated, и пользователь автоматически
    // перенаправляется на экран входа без ручного вызова навигации.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigateClearingBackStack(Routers.Home.route)
            }
            is AuthState.Unauthenticated -> {
                navController.navigateClearingBackStack(Routers.UserAuth.route)
            }
            // Loading и Error: остаёмся на Splash, пока состояние не станет определённым
            else -> {}
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
                    navController.navigateClearingBackStack(Routers.Home.route)
                }
            )
        }

        composable(Routers.Home.route) {
            HomeScreen(
                onOpenUser = {
                    navController.navigate(Routers.UserInfo.route)
                },
                onDstPointsList = {
                    navController.navigate(Routers.DstPointsList.route)
                },
                onLogout = onLogout
            )
        }

        composable(Routers.UserInfo.route) {
            UserScreen(
                onBack = {
                    navController.popBackStack()
                },
                viewModel = hiltViewModel()
            )
        }

        composable(Routers.DstPointsList.route) {
            DstPointListScreen(
                onBack = {
                    navController.popBackStack()
                },
                onDstPoint = { zoneId ->
                    navController.navigate(Routers.DstPoint.createRoute(zoneId))
                },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Routers.DstPoint.route,
            arguments = listOf(
                navArgument(Routers.DstPoint.ARG_ZONE_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val zoneId = backStackEntry.arguments
                ?.getInt(Routers.DstPoint.ARG_ZONE_ID)
                ?: return@composable

            DstPointScreen(
                onBack = {
                    navController.popBackStack()
                },
                zoneId = zoneId,
                viewModel = hiltViewModel()
            )
        }
    }
}