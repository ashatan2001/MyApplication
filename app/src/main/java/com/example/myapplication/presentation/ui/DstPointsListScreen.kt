package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DstPoint
import com.example.myapplication.presentation.viewmodel.DstPointUiState
import com.example.myapplication.presentation.viewmodel.DstPointViewModel
import com.example.myapplication.presentation.viewmodel.HomeEvent

/**
 * Экран вывода информации о точках выгрузки бетона.
 *
 * @param onBack Навигация назад на домашний экран.
 * @param viewModel [DstPointViewModel] для загрузки данных о точках выгрузки бетона.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DstPointListScreen(
    onBack: () -> Unit,
    onDstPoint: () -> Unit,
    viewModel: DstPointViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var zoneId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadDstPointData()

        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToLogin -> {
                    onLogout()
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Список точек выгрузки бетона",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = zoneId,
                    onValueChange = { zoneId = it },
                    label = { Text("Код подразделения") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = {
                        zoneId.toIntOrNull()?.let { id ->
                            viewModel.loadDstPointData(id)
                        }
                    },
                    enabled = zoneId.isNotBlank()
                ) {
                    Text("Найти")
                }
            }
        }
    }
}