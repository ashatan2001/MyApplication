package com.example.myapplication.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.AuthState
import com.example.myapplication.presentation.ui.*
import com.example.myapplication.presentation.util.LayoutSize.NavigationRailWidth
import com.example.myapplication.presentation.util.WindowSize
import com.example.myapplication.presentation.util.rememberWindowSize
import com.example.myapplication.presentation.viewmodel.SessionViewModel

/**
 * Главная оболочка приложения (Shell), обеспечивающая постоянство UI-элементов.
 *
 * Адаптивная навигация:
 * - на телефонах ([WindowSize.COMPACT]) — нижняя панель [MainBottomBar];
 * - на планшетах ([WindowSize.MEDIUM], [WindowSize.EXPANDED]) — боковая панель [MainNavigationRail].
 *
 * Базовые разделы (Заказ, Очередь, Архив) блокируются (становятся светло-серыми)
 * при отсутствии авторизации ([AuthState.Authenticated]).
 *
 * @param onLogout Колбэк для выхода из системы.
 * @param onLogin Колбэк для перехода на экран авторизации.
 * @param sessionViewModel ViewModel для управления сессией пользователя.
 */
@Composable
fun MainShell(
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val windowSize = rememberWindowSize()
    val useSideNavigation = windowSize != WindowSize.COMPACT
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Определяем, авторизован ли пользователь.
    val authState by sessionViewModel.authState.collectAsStateWithLifecycle()
    val isAuthenticated = authState is AuthState.Authenticated

    // Определяем, является ли текущий экран корневым (для скрытия кнопки "Назад" в TopBar)
    val isRootScreen = currentRoute in listOf(
        //Routers.Home.route,
        Routers.DstPointList.route,
        Routers.BucketList.route,
        Routers.Queue.route,
        Routers.Archive.route
    )

    /**
     * Единый обработчик переключения разделов верхнего уровня
     * с сохранением состояния каждого раздела (saveState/restoreState).
     */
    val navigateToTopLevel: (String) -> Unit = { route ->
        innerNavController.navigate(route) {
            popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                isRootScreen = isRootScreen,
                currentRoute = currentRoute,
                useSideNavigation = useSideNavigation,
                isAuthenticated = isAuthenticated,
                onBackClick = { innerNavController.popBackStack() },
                onSettingClick = { innerNavController.navigate(Routers.Settings.route) },
                onProfileClick = { innerNavController.navigate(Routers.PersonInfo.route) },
                onLoginClick = onLogin,
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            if (!useSideNavigation) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    isAuthenticated = isAuthenticated,
                    //onHomeClick = { navigateToTopLevel(Routers.Home.route) },
                    onDstPointListClick = { navigateToTopLevel(Routers.DstPointList.route) },
                    onBucketListClick = { navigateToTopLevel(Routers.BucketList.route) },
                    onQueueClick = { navigateToTopLevel(Routers.Queue.route) },
                    onArchiveClick = { navigateToTopLevel(Routers.Archive.route) }
                )
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (useSideNavigation) {
                MainNavigationRail(
                    currentRoute = currentRoute,
                    isAuthenticated = isAuthenticated,
                    //onHomeClick = { navigateToTopLevel(Routers.Home.route) },
                    onDstPointListClick = { navigateToTopLevel(Routers.DstPointList.route) },
                    onBucketListClick = { navigateToTopLevel(Routers.BucketList.route) },
                    onQueueClick = { navigateToTopLevel(Routers.Queue.route) },
                    onArchiveClick = { navigateToTopLevel(Routers.Archive.route) }
                )
            }

            NavHost(
                navController = innerNavController,
                startDestination = Routers.Home.route,
                modifier = Modifier.weight(1f).fillMaxSize()
            ) {
                composable(Routers.Home.route) { HomeScreen() }

                composable(Routers.DstPointList.route) {
                    DstPointListScreen(
                        onProductList = { zoneId ->
                            innerNavController.navigate(Routers.ProductList.createRoute(zoneId))
                        }
                    )
                }

                composable(Routers.Queue.route) { QueueScreen() }

                composable(Routers.BucketList.route) { BucketScreen() }

                composable(Routers.Archive.route) { ArchiveScreen() }

                composable(Routers.PersonInfo.route) {
                    PersonScreen(
                        onBack = { innerNavController.popBackStack() },
                        viewModel = hiltViewModel()
                    )
                }

                composable(Routers.Settings.route) {
                    SettingsScreen(
                        showTopBar = false,
                        onNavigateToAuth = {
                            if (isAuthenticated) {
                                innerNavController.popBackStack()
                            } else {
                                onLogin()
                            }
                        },
                        onBack = { innerNavController.popBackStack() }
                    )
                }

                composable(
                    route = Routers.ProductList.route,
                    arguments = listOf(
                        navArgument(Routers.ProductList.ARG_ZONE_ID) { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val zone = backStackEntry.arguments?.getInt(Routers.ProductList.ARG_ZONE_ID)
                        ?: return@composable
                    ProductListScreen(
                        onBack = { innerNavController.popBackStack() },
                        zone = zone,
                        viewModel = hiltViewModel()
                    )
                }

                composable(
                    route = Routers.DstPoint.route,
                    arguments = listOf(
                        navArgument(Routers.DstPoint.ARG_ZONE_ID) { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val zoneId = backStackEntry.arguments?.getInt(Routers.DstPoint.ARG_ZONE_ID)
                        ?: return@composable
                    DstPointScreen(
                        onBack = { innerNavController.popBackStack() },
                        zoneId = zoneId,
                        viewModel = hiltViewModel()
                    )
                }
            }
        }
    }
}

/**
 * Боковая навигационная панель для планшетов.
 *
 * @param currentRoute Текущий активный маршрут.
 * @param isAuthenticated Признак авторизации. Если false, недоступные разделы
 *                        отображаются светло-серыми и игнорируют клики.
 * @param onHomeClick Колбэк перехода на главную.
 * @param onDstPointListClick Колбэк перехода к списку точек выгрузки.
 * @param onBucketListClick Колбэк перехода к мониторингу состояния бетоновозных телег.
 * @param onQueueClick Колбэк перехода к очереди заявок.
 * @param onArchiveClick Колбэк перехода к архивам.
 */
@Composable
private fun MainNavigationRail(
    currentRoute: String?,
    isAuthenticated: Boolean,
    //onHomeClick: () -> Unit,
    onDstPointListClick: () -> Unit,
    onBucketListClick: () -> Unit,
    onQueueClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    NavigationRail(modifier = Modifier.width(NavigationRailWidth)) {
        /*NavigationRailItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label = { Text("Главная", fontSize = 12.sp) },
            selected = currentRoute == Routers.Home.route,
            onClick = onHomeClick
        )*/
        NavigationRailItem(
            icon = { Icon(Icons.Default.LibraryAdd, contentDescription = "Заказ") },
            label = { Text("Заказ", fontSize = 12.sp) },
            selected = currentRoute == Routers.DstPointList.route ||
                    currentRoute?.startsWith("product_list") == true,
            onClick = onDstPointListClick,
            enabled = isAuthenticated
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.PendingActions, contentDescription = "Очередь") },
            label = { Text("Очередь", fontSize = 12.sp) },
            selected = currentRoute == Routers.Queue.route,
            onClick = onQueueClick,
            enabled = isAuthenticated
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.LibraryAdd, contentDescription = "Телеги") },
            label = { Text("Телеги", fontSize = 12.sp) },
            selected = currentRoute == Routers.BucketList.route,
            onClick = onBucketListClick,
            enabled = isAuthenticated
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.Archive, contentDescription = "Архив") },
            label = { Text("Архив", fontSize = 12.sp) },
            selected = currentRoute == Routers.Archive.route,
            onClick = onArchiveClick,
            enabled = isAuthenticated
        )
    }
}

/**
 * Верхняя панель приложения.
 * Динамически отображает кнопку "Назад" только для вложенных экранов.
 * При отсутствии авторизации вместо меню "Профиль/Выйти" показывает кнопку "Войти".
 *
 * @param isRootScreen Признак корневого экрана (не показывать "Назад").
 * @param currentRoute Текущий маршрут для формирования заголовка.
 * @param useSideNavigation Используется ли боковая панель (для отступов).
 * @param isAuthenticated Признак авторизации пользователя.
 * @param onBackClick Колбэк кнопки "Назад".
 * @param onSettingClick Колбэк перехода к настройкам.
 * @param onProfileClick Колбэк перехода к профилю.
 * @param onLoginClick Колбэк перехода к экрану входа.
 * @param onLogoutClick Колбэк выхода из системы.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    isRootScreen: Boolean,
    currentRoute: String?,
    useSideNavigation: Boolean,
    isAuthenticated: Boolean,
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    /** Отступ слева, компенсирующий ширину NavigationRail на планшетах. */
    val startPadding = if (useSideNavigation) NavigationRailWidth else 0.dp

    TopAppBar(
        title = {
            Text(
                text = getScreenTitle(currentRoute),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            if (!isRootScreen) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(start = startPadding)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            } else {
                Spacer(modifier = Modifier.width(startPadding + 48.dp))
            }
        },
        actions = {
            IconButton(onClick = onSettingClick) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }

            if (isAuthenticated) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Ещё")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Профиль") },
                            onClick = {
                                menuExpanded = false
                                onProfileClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Выйти") },
                            onClick = {
                                menuExpanded = false
                                onLogoutClick()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            }
                        )
                    }
                }
            } else {
                IconButton(onClick = onLoginClick) {
                    Icon(Icons.Default.Login, contentDescription = "Войти")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Нижняя навигационная панель для телефонов.
 *
 * @param currentRoute Текущий активный маршрут.
 * @param isAuthenticated Признак авторизации. Если false, недоступные разделы
 *                        отображаются светло-серыми и игнорируют клики.
 * @param onHomeClick Колбэк перехода на главную.
 * @param onDstPointListClick Колбэк перехода к списку точек выгрузки.
 * @param onQueueClick Колбэк перехода к очереди заявок.
 * @param onBucketListClick Колбэк перехода к мониторингу состояния бетоновозных телег.
 * @param onArchiveClick Колбэк перехода к архивам.
 */
@Composable
private fun MainBottomBar(
    currentRoute: String?,
    isAuthenticated: Boolean,
    //onHomeClick: () -> Unit,
    onDstPointListClick: () -> Unit,
    onQueueClick: () -> Unit,
    onBucketListClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    NavigationBar {
        /*NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label = { Text("Главная") },
            selected = currentRoute == Routers.Home.route,
            onClick = onBucketListClick
        )*/
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryAdd, contentDescription = "Заказ") },
            label = { Text("Заказ") },
            selected = currentRoute == Routers.DstPointList.route ||
                    currentRoute?.startsWith("product_list") == true,
            onClick = onDstPointListClick,
            enabled = isAuthenticated
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PendingActions, contentDescription = "Очередь") },
            label = { Text("Очередь") },
            selected = currentRoute == Routers.Queue.route,
            onClick = onQueueClick,
            enabled = isAuthenticated
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Телеги") },
            label = { Text("Телеги") },
            selected = currentRoute == Routers.BucketList.route,
            onClick = onBucketListClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Archive, contentDescription = "Архив") },
            label = { Text("Архив") },
            selected = currentRoute == Routers.Archive.route,
            onClick = onArchiveClick,
            enabled = isAuthenticated
        )
    }
}

/**
 * Возвращает заголовок экрана с учётом класса размера окна.
 *
 * @param route Текущий маршрут.
 * @return Локализованная строка заголовка.
 */
@Composable
private fun getScreenTitle(route: String?): String {
    val isCompact = rememberWindowSize() == WindowSize.COMPACT
    return when (route) {
        Routers.Home.route -> "Главная"
        Routers.DstPointList.route -> if (isCompact) "Зоны выгрузки" else "Зоны выгрузки бетона"
        Routers.ProductList.route -> if (isCompact) "Продукты" else "Продукты для отгрузки"
        Routers.Queue.route -> if (isCompact) "Очередь" else "Очередь заявок на отгрузку"
        Routers.BucketList.route -> if (isCompact) "Телеги" else "Бетоновозные телеги"
        Routers.Archive.route -> "Архивы"
        Routers.PersonInfo.route -> "Профиль"
        Routers.Settings.route -> "Настройки сервера"
        else -> ""
    }
}