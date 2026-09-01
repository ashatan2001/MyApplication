package com.example.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    onOpenUser: (Int) -> Unit,
    onLogout: () -> Unit
) {
    // 1. Состояние для боковой панели
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val viewModel: HomeViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    // 2. Обертка с боковой панелью
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Содержимое боковой панели
            DrawerContent(
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                onOpenUser = onOpenUser,
                onLogout = onLogout
            )
        },
        gesturesEnabled = true
    ) {
        // 3. Основной контент с верхней панелью
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Главная") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Открыть меню")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Главная страница",
                    modifier = Modifier.padding(20.dp),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}

// 4. Содержимое боковой панели
@Composable
fun DrawerContent(
    onCloseDrawer: () -> Unit,
    onOpenUser: (Int) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "Меню",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        // Пункты меню
        DrawerItem(
            icon = Icons.Default.Person,
            text = "Профиль пользователя",
            onClick = {
                onCloseDrawer()
                onOpenUser(1) // Открываем профиль
            }
        )

        DrawerItem(
            icon = Icons.Default.Settings,
            text = "Настройки",
            onClick = {
                onCloseDrawer()
                // Здесь настройки
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // 4. Пункт выхода (внизу)
        DrawerItem(
            icon = Icons.Default.ExitToApp,
            text = "Выйти",
            onClick = {
                onCloseDrawer()
                onLogout()
            }
        )
    }
}

// 5. Компонент пункта меню
@Composable
fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
fun HomePreview() = HomeScreen(
    onOpenUser = {},
    onLogout = {}
)