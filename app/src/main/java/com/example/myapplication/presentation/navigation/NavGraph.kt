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
import com.example.domain.usecase.ObserveServerUrlUseCase
import com.example.myapplication.presentation.ui.AuthScreen
import com.example.myapplication.presentation.ui.SettingsScreen
import com.example.myapplication.presentation.viewmodel.SessionViewModel
import com.example.myapplication.presentation.viewmodel.SettingsEvent
import com.example.myapplication.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel для корневой навигации.
 * Предоставляет состояние наличия URL сервера для NavGraph.
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    observeServerUrlUseCase: ObserveServerUrlUseCase
) : androidx.lifecycle.ViewModel() {
    val serverUrl = observeServerUrlUseCase()
}

/**
 * Корневой граф навигации приложения.
 *
 * Логика редиректов:
 * 1. Если URL сервера отсутствует → редирект на [Routers.Settings]
 * 2. Если URL существует, но пользователь не авторизован → редирект на [Routers.UserAuth]
 * 3. Если пользователь авторизован → редирект на [Routers.MainShell]
 *
 * @param navController Контроллер навигации.
 * @param onLogout Колбэк для выхода из системы.
 * @param onLogin Колбэк для перехода на экран авторизации.
 * @param sessionViewModel ViewModel для наблюдения за состоянием сессии.
 * @param navigationViewModel ViewModel для наблюдения за URL сервера.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    sessionViewModel: SessionViewModel = hiltViewModel(),
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    val authState by sessionViewModel.authState.collectAsStateWithLifecycle()
    val serverUrl by navigationViewModel.serverUrl.collectAsStateWithLifecycle(initialValue = "")

    val hasServerUrl = serverUrl.isNotBlank()

    // Логика автоматических редиректов
    LaunchedEffect(authState, hasServerUrl) {
        val currentRoute = navController.currentDestination?.route

        when {
            // Нет URL сервера → на экран настроек
            !hasServerUrl && currentRoute == Routers.Splash.route -> {
                navController.navigateClearingBackStack(Routers.Settings.route)
            }
            // Есть URL, но не авторизован → на экран входа
            hasServerUrl && authState is AuthState.Unauthenticated && currentRoute == Routers.Splash.route -> {
                navController.navigateClearingBackStack(Routers.UserAuth.route)
            }
            // Авторизован → в MainShell
            authState is AuthState.Authenticated && currentRoute == Routers.Splash.route -> {
                navController.navigateClearingBackStack(Routers.MainShell.route)
            }
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
                    // После успешной авторизации переходим в MainShell
                    navController.navigateClearingBackStack(Routers.MainShell.route)
                },
                onNavigateToSettings = {
                    // Переход на экран настроек (кнопка "Изменить сервер")
                    navController.navigate(Routers.Settings.route)
                }
            )
        }

        composable(Routers.Settings.route) {
            SettingsScreen(
                onNavigateToAuth = {
                    // После сохранения URL переходим на экран авторизации
                    navController.navigateClearingBackStack(Routers.UserAuth.route)
                },
                onBack = {
                    // Кнопка "Назад" работает только если есть URL (можно вернуться в AuthScreen)
                    navController.popBackStack()
                }
            )
        }

        composable(Routers.MainShell.route) {
            MainShell(
                onLogout = onLogout,
                onLogin = onLogin
            )
        }
    }
}