package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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

/**
 * Экран вывода информации о точках выгрузки бетона.
 *
 * @param onBack Навигация назад на домашний экран.
 * @param viewModel [DstPointViewModel] для загрузки данных о точках выгрузки бетона.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DstPointScreen(
    onBack: () -> Unit,
    viewModel: DstPointViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var zoneId by remember { mutableStateOf("") }

    @Composable
    fun DstPointScreen(
        onBack: () -> Unit,
        viewModel: DstPointViewModel = hiltViewModel()
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        var zoneId by remember { mutableStateOf("") }

        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (val state = uiState) {
                    is DstPointUiState.Loading -> CircularProgressIndicator()
                    is DstPointUiState.Success -> DstPointContent(dstPoint = state.dstPoint)
                    is DstPointUiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = { viewModel.loadDstPointData() }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Основной контент экрана с информацией о точках загрузки
 */
@Composable
private fun DstPointContent(dstPoint: DstPoint) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Код подразделения", style = MaterialTheme.typography.labelSmall)
                    Text(dstPoint.departmentId, style = MaterialTheme.typography.bodyLarge)
                }

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        }
    }
}