package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DstPoint
import com.example.myapplication.presentation.viewmodel.DstPointUiState
import com.example.myapplication.presentation.viewmodel.DstPointViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DstPointScreen(
    onBack: () -> Unit,
    zoneId: Int,
    viewModel: DstPointViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(zoneId) {
        viewModel.loadDstPointData(zoneId)
    }

    Scaffold (topBar = {
        TopAppBar(
            title = {
                Text("Список точек выгрузки бетона")
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }
        )
    }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {

                is DstPointUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DstPointUiState.Success -> {
                    DstPointContent(
                        dstPoint = state.dstPoint
                    )
                }

                is DstPointUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(
                            onClick = {
                                viewModel.loadDstPointData(zoneId)
                            }
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }
        }
    }
}

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = dstPoint.zoneName,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = dstPoint.departmentId,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    dstPoint.postId?.let { postId ->
                        Text(
                            text = postId,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

            }
        }
    }
}