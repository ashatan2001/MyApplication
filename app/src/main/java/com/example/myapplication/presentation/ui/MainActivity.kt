package com.example.myapplication.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.presentation.navigation.NavGraph
import com.example.myapplication.presentation.navigation.Routers
import com.example.myapplication.presentation.navigation.lightStatusBarRoutes
import com.example.myapplication.presentation.navigation.navigateClearingBackStack
import com.example.myapplication.presentation.util.StatusBarIconAppearance
import com.example.myapplication.presentation.viewmodel.SessionViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Точка входа в приложение.
 *
 * Работает в режиме edge-to-edge: системные insets обрабатываются только
 * внутренними оболочками ([MainShell]: TopAppBar, NavigationBar/Rail),
 * поэтому внешний [Scaffold] имеет нулевые [Scaffold.contentWindowInsets],
 * чтобы не добавлять отступы статус-бара и навигации повторно.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: SessionViewModel = hiltViewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    viewModel.logoutEvents.collect {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Сессия истекла. Пожалуйста, войдите снова",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.navigationBarsPadding()
                    ) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()

                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = backStackEntry?.destination?.route

                        StatusBarIconAppearance(lightIcons = currentRoute !in lightStatusBarRoutes)

                        NavGraph(
                            navController = navController,
                            onLogout = {
                                // Выход из системы: полная очистка стека и переход на экран авторизации
                                navController.navigateClearingBackStack(Routers.UserAuth.route)
                            }
                        )
                    }
                }
            }
        }
    }
}