package com.example.myapplication.presentation.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.viewmodel.HomeUiState
import com.example.myapplication.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenUser: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUserId = (uiState as? HomeUiState.Success)?.userId ?: 0

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onOpenUser = onOpenUser,
                onLogout = {
                    viewModel.logout {
                        onLogout()
                    }
                },
                currentUserId = currentUserId
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Главная") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
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
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> CircularProgressIndicator()
                    is HomeUiState.Success -> Text("Привет, ${state.userName}!")
                    is HomeUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    onCloseDrawer: () -> Unit,
    onOpenUser: (Int) -> Unit,
    onLogout: () -> Unit,
    currentUserId: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text("Меню", style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp))
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        DrawerItem(Icons.Default.Person, "Профиль") {
            onCloseDrawer()
            if (currentUserId > 0) onOpenUser(currentUserId)
        }
        DrawerItem(Icons.Default.ExitToApp, "Выйти") {
            onCloseDrawer()
            onLogout()
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}