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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.domain.model.AuthState
import com.example.domain.usecase.ObserveServerUrlUseCase
import com.example.myapplication.presentation.ui.AuthScreen
import com.example.myapplication.presentation.ui.SettingsScreen
import com.example.myapplication.presentation.viewmodel.SessionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel корневой навигации.
 *
 * Предоставляет URL сервера с явным состоянием загрузки, чтобы стартовый
 * редирект не принимал решений на основе ещё не прочитанных данных.
 *
 * @param observeServerUrlUseCase UseCase наблюдения за сохранённым URL сервера.
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    observeServerUrlUseCase: ObserveServerUrlUseCase
) : ViewModel() {

    /**
     * URL сервера с различимым состоянием «ещё не загружено».
     *
     * semantics значений:
     * - `null` — асинхронное чтение DataStore ещё не завершилось (стартовое значение);
     * - непустая строка — сервер настроен (сохранённый URL либо BuildConfig.BASE_URL);
     * - пустая строка — сервер действительно не задан.
     *
     * [SharingStarted.WhileSubscribed] с таймаутом 5 с переживает смену конфигурации
     * без повторного чтения DataStore.
     */
    val serverUrl: StateFlow<String?> = observeServerUrlUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}

/**
 * Корневой граф навигации приложения.
 *
 * Логика редиректов (выполняется ТОЛЬКО после того, как и состояние сессии,
 * и URL сервера получили реальные значения — иначе остаёмся на Splash):
 * 1. URL сервера отсутствует → [Routers.Settings];
 * 2. URL есть, пользователь не авторизован → [Routers.UserAuth];
 * 3. Пользователь авторизован → [Routers.MainShell];
 * 4. Сессия завершилась во время работы в защищённой зоне → [Routers.UserAuth].
 *
 * @param navController Контроллер навигации.
 * @param onLogout Колбэк выхода из системы (запускает очистку сессии).
 * @param onLogin Колбэк перехода на экран авторизации.
 * @param sessionViewModel ViewModel наблюдения за состоянием сессии.
 * @param navigationViewModel ViewModel наблюдения за URL сервера.
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
    val serverUrl by navigationViewModel.serverUrl.collectAsStateWithLifecycle()

    // Текущий маршрут внешнего графа — ключ эффекта, чтобы редирект
    // гарантированно переоценивался и после установки стартового назначения.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Готовность данных: Loading у сессии и null у URL означают «ещё читаем».
    val isAuthResolved = authState !is AuthState.Loading
    val isServerUrlResolved = serverUrl != null
    val hasServerUrl = !serverUrl.isNullOrBlank()

    /**
     * Автоматические редиректы. Пока хотя бы один источник не готов —
     * никакого навигационного решения (защита от гонки холодного старта,
     * из-за которой приложение открывалось на экране ввода URL).
     * После навигации эффект перезапускается по смене currentRoute,
     * но ни одна ветка не совпадает с целевым маршрутом — цикла нет.
     */
    LaunchedEffect(authState, serverUrl, currentRoute) {
        if (!isAuthResolved || !isServerUrlResolved) return@LaunchedEffect

        when {
            // Сервер действительно не настроен → экран настроек
            !hasServerUrl && currentRoute == Routers.Splash.route -> {
                navController.navigateClearingBackStack(Routers.Settings.route)
            }
            // Сервер есть, входа нет → экран авторизации
            hasServerUrl && authState is AuthState.Unauthenticated &&
                    currentRoute == Routers.Splash.route -> {
                navController.navigateClearingBackStack(Routers.UserAuth.route)
            }
            // Сессия активна → рабочая оболочка
            authState is AuthState.Authenticated && currentRoute == Routers.Splash.route -> {
                navController.navigateClearingBackStack(Routers.MainShell.route)
            }
            // Сессия истекла/завершена внутри защищённой зоны → экран входа
            hasServerUrl && authState is AuthState.Unauthenticated &&
                    currentRoute == Routers.MainShell.route -> {
                navController.navigateClearingBackStack(Routers.UserAuth.route)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routers.Splash.route
    ) {
        // Экран ожидания: виден ровно до готовности данных сессии и URL.
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
                    // Переход на экран настроек (кнопка «Изменить сервер»)
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
                    // Возврат возможен, если экран настроек открыт поверх графа
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