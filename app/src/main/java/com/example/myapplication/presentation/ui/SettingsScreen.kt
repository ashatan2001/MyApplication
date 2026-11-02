package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.viewmodel.SettingsUiState
import com.example.myapplication.presentation.viewmodel.SettingsViewModel

/**
 * Экран настроек сервера.
 *
 * Позволяет пользователю изменить URL API-сервера или сбросить его
 * к значению по умолчанию. Изменения применяются немедленно для всех
 * новых запросов благодаря DynamicBaseUrlInterceptor.
 *
 * Экран не содержит собственной верхней панели: заголовок и кнопка «Назад»
 * отображаются общей панелью [com.example.myapplication.presentation.navigation.MainShell],
 * что исключает дублирование AppBar.
 *
 * @param viewModel [SettingsViewModel] для управления состоянием экрана.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /** Локальное состояние текстового поля ввода URL. */
    var urlInput by remember { mutableStateOf("") }

    // Синхронизируем локальное поле ввода с состоянием из ViewModel
    LaunchedEffect(uiState) {
        if (uiState is SettingsUiState.Success) {
            urlInput = (uiState as SettingsUiState.Success).currentUrl
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Укажите адрес API-сервера. Изменения вступят в силу немедленно для всех новых запросов.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("URL сервера") },
            placeholder = { Text("http://10.0.2.2:3080/") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true,
            enabled = uiState !is SettingsUiState.Saving
        )

        // Отображение ошибок валидации и сохранения
        if (uiState is SettingsUiState.Error) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = (uiState as SettingsUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопки действий
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.resetToDefault() },
                modifier = Modifier.weight(1f),
                enabled = uiState !is SettingsUiState.Saving
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("По умолчанию")
            }

            Button(
                onClick = { viewModel.saveUrl(urlInput) },
                modifier = Modifier.weight(1f),
                enabled = uiState !is SettingsUiState.Saving
            ) {
                if (uiState is SettingsUiState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранить")
                }
            }
        }
    }
}