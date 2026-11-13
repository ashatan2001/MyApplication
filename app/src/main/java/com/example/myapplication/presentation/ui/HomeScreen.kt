package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.viewmodel.HomeEvent
import com.example.myapplication.presentation.viewmodel.HomeUiState
import com.example.myapplication.presentation.viewmodel.HomeViewModel

/**
 * Экран главной панели управления (Dashboard).
 *
 * Упрощен до отображения только контента, так как [Scaffold] и панели навигации
 * теперь управляются на уровне [MainShell]. Отвечает за загрузку данных пользователя
 * и отображение приветствия или состояния ошибки.
 *
 * @param viewModel [HomeViewModel] для управления состоянием и бизнес-логикой экрана.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
        viewModel.events.collect { event ->
            // События глобальной навигации (например, выход) обрабатываются на уровне MainShell
            when (event) {
                is HomeEvent.NavigateToLogin -> { /* Обработано в MainShell */ }
            }
        }
    }

    HomeContent(uiState = uiState)
}

/**
 * Отображение контента главного экрана в зависимости от текущего состояния UI.
 *
 * @param uiState Текущее состояние экрана ([HomeUiState]).
 */
@Composable
private fun HomeContent(uiState: HomeUiState) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (uiState) {
            is HomeUiState.Loading -> CircularProgressIndicator()
            is HomeUiState.Success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.Sm)
                ) {
                    Text("Привет, ${uiState.personName}!", style = MaterialTheme.typography.headlineMedium)
                }
            }
            is HomeUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.Md)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(IconSize.Xl)
                    )
                    Text(uiState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}