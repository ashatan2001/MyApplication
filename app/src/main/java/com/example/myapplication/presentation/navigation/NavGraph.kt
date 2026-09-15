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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.AuthState
import com.example.myapplication.presentation.ui.AuthScreen
import com.example.myapplication.presentation.ui.HomeScreen
import com.example.myapplication.presentation.ui.UserScreen
import com.example.myapplication.presentation.viewmodel.SessionViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object UserInfo : Screen("user_info") {
    }
    data object UserAuth : Screen("user_auth")
}

@Composable
fun NavGraph(
    viewModel: SessionViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.logoutEvents.collect {
            navController.navigate(Screen.UserAuth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

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

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable(Screen.UserAuth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UserAuth.route) { inclusive = true }
                    }
                },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onOpenUser = {
                    navController.navigate(Screen.UserInfo.route)
                },
                onLogout = {
                    navController.navigate(Screen.UserAuth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserInfo.route) {
            UserScreen(
                onBack = {
                    navController.navigate(Screen.Home.route)
                },
                viewModel = hiltViewModel()
            )
        }
    }
}