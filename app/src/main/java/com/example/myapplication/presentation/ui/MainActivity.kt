package com.example.myapplication.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.presentation.navigation.NavGraph
import com.example.myapplication.presentation.navigation.Routers
import com.example.myapplication.presentation.navigation.navigateClearingBackStack
import com.example.myapplication.presentation.viewmodel.SessionViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Точка входа в приложение.
 *
 * Инициализирует тему, навигацию и глобальный [SessionViewModel]
 * для отслеживания событий выхода из системы.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val viewModel: SessionViewModel = hiltViewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                /**
                 * Показ Snackbar при принудительном выходе (истечение сессии, логаут).
                 * LaunchedEffect(Unit) гарантирует, что подписка активна на всё время жизни активности.
                 */
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
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()
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