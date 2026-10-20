package com.example.myapplication.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.presentation.ui.*
import com.example.myapplication.presentation.viewmodel.SessionViewModel

/**
 * Главная оболочка приложения (Shell), обеспечивающая постоянство UI-элементов.
 *
 * Содержит единый [Scaffold] с закрепленными верхней панелью ([MainTopBar])
 * и нижней навигационной панелью ([MainBottomBar]). Внутри размещает вложенный
 * [NavHost] для переключения между основными экранами приложения без перерисовки панелей.
 *
 * @param onLogout Колбэк для завершения сессии пользователя.
 * @param sessionViewModel [SessionViewModel] для глобального управления сессией.
 */
@Composable
fun MainShell(
    onLogout: () -> Unit,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Определяем, является ли текущий экран корневым (для скрытия кнопки "Назад" в TopBar)
    val isRootScreen = currentRoute in listOf(
        Routers.Home.route,
        Routers.DstPointList.route,
        Routers.Queue.route,
        Routers.Archive.route
    )

    Scaffold(
        topBar = {
            MainTopBar(
                isRootScreen = isRootScreen,
                currentRoute = currentRoute,
                onBackClick = { innerNavController.popBackStack() },
                onSettingClick = { innerNavController.navigate(Routers.PersonInfo.route) },
                onProfileClick = { innerNavController.navigate(Routers.PersonInfo.route) },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            MainBottomBar(
                currentRoute = currentRoute,
                onHomeClick = {
                    innerNavController.navigate(Routers.Home.route) {
                        popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onDstPointListClick = {
                    innerNavController.navigate(Routers.DstPointList.route) {
                        popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onQueueClick = {
                    innerNavController.navigate(Routers.Queue.route) {
                        popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onArchiveClick = {
                    innerNavController.navigate(Routers.Archive.route) {
                        popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Routers.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
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

            composable(Routers.Archive.route) { ArchiveScreen() }

            composable(Routers.PersonInfo.route) {
                PersonScreen(
                    onBack = { innerNavController.popBackStack() },
                    viewModel = hiltViewModel()
                )
            }

            composable(
                route = Routers.ProductList.route,
                arguments = listOf(navArgument(Routers.ProductList.ARG_ZONE_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val zone = backStackEntry.arguments?.getInt(Routers.ProductList.ARG_ZONE_ID) ?: return@composable
                ProductListScreen(
                    onBack = { innerNavController.popBackStack() },
                    zone = zone,
                    viewModel = hiltViewModel()
                )
            }

            composable(
                route = Routers.DstPoint.route,
                arguments = listOf(navArgument(Routers.DstPoint.ARG_ZONE_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val zoneId = backStackEntry.arguments?.getInt(Routers.DstPoint.ARG_ZONE_ID) ?: return@composable
                DstPointScreen(
                    onBack = { innerNavController.popBackStack() },
                    zoneId = zoneId,
                    viewModel = hiltViewModel()
                )
            }
        }
    }
}

/**
 * Верхняя панель приложения.
 *
 * Динамически отображает кнопку "Назад" только для вложенных экранов.
 * Кнопки профиля и выхода доступны на всех экранах внутри оболочки.
 *
 * @param isRootScreen Флаг, указывающий, является ли текущий экран корневым.
 * @param currentRoute Текущий активный маршрут для отображения заголовка.
 * @param onBackClick Действие при нажатии на кнопку "Назад".
 * @param onSettingClick Действие при нажатии на кнопку настройки.
 * @param onProfileClick Действие при нажатии на кнопку профиля.
 * @param onLogoutClick Действие при нажатии на кнопку выхода.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    isRootScreen: Boolean,
    currentRoute: String?,
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit

) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = getScreenTitle(currentRoute),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            if (!isRootScreen) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }
            else Spacer(modifier = Modifier.size(48.dp))
        },
        actions = {
            IconButton(onClick = onSettingClick) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Ещё"
                    )
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
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null
                            )
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Выйти") },
                        onClick = {
                            menuExpanded = false
                            onLogoutClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null
                            )
                        }
                    )
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
 * Нижняя навигационная панель.
 *
 * Обеспечивает переключение между тремя основными разделами приложения.
 * Использует `saveState` и `restoreState` для сохранения состояния прокрутки и данных при переключении.
 *
 * @param currentRoute Текущий активный маршрут для подсветки выбранного элемента.
 * @param onHomeClick Действие при выборе вкладки "Главная".
 * @param onDstPointListClick Действие при выборе вкладки "Заказ".
 * @param onQueueClick Действие при выборе вкладки "Очередь".
 * @param onArchiveClick Действие при выборе вкладки "Архив".
 */
@Composable
private fun MainBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onDstPointListClick: () -> Unit,
    onQueueClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label = { Text("Главная") },
            selected = currentRoute == Routers.Home.route,
            onClick = onHomeClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryAdd, contentDescription = "Заказ") },
            label = { Text("Заказ") },
            selected = currentRoute == Routers.DstPointList.route || currentRoute?.startsWith("product_list") == true,
            onClick = onDstPointListClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PendingActions, contentDescription = "Очередь") },
            label = { Text("Очередь") },
            selected = currentRoute == Routers.Queue.route,
            onClick = onQueueClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Archive, contentDescription = "Архив") },
            label = { Text("Архив") },
            selected = currentRoute == Routers.Archive.route,
            onClick = onArchiveClick
        )
    }
}

/**
 * Вспомогательная функция для получения заголовка экрана на основе текущего маршрута.
 *
 * @param route Строковое представление текущего маршрута.
 * @return Строка с названием экрана для отображения в [TopAppBar].
 */
private fun getScreenTitle(route: String?): String = when (route) {
    Routers.Home.route -> "Главная"
    Routers.DstPointList.route -> "Зоны выгрузки бетона"
    Routers.ProductList.route -> "Продукты для отгрузки"
    Routers.Queue.route -> "Очередь заявок на отгрузку"
    Routers.Archive.route -> "Архивы"
    Routers.PersonInfo.route -> "Профиль"
    else -> ""
}