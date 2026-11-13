package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.viewmodel.ServerConnectionStatus
import com.example.myapplication.presentation.viewmodel.SettingsEvent
import com.example.myapplication.presentation.viewmodel.SettingsUiState
import com.example.myapplication.presentation.viewmodel.SettingsViewModel

/**
 * Экран настроек сервера.
 *
 * Позволяет изменить URL API-сервера, протестировать подключение или сбросить
 * настройки к значению по умолчанию. Изменения применяются немедленно для всех
 * новых запросов благодаря DynamicBaseUrlInterceptor.
 *
 * Экран поддерживает два режима отображения, чтобы избежать «двойной верхней панели»:
 * - автономный ([showTopBar] = true): переход из корневого графа (Splash / экран
 *   авторизации) — экран сам рисует [Scaffold] с [TopAppBar] и кнопкой «Назад»;
 * - встроенный ([showTopBar] = false): переход из MainShell — экран рисует только
 *   контент, а верхнюю панель и стрелку «Назад» предоставляет оболочка.
 *
 * После успешного сохранения URL отправляется событие [SettingsEvent.NavigateToAuth]
 * для перехода на экран авторизации.
 *
 * @param onNavigateToAuth Колбэк для перехода на экран авторизации после сохранения URL.
 * @param onBack Колбэк для кнопки «Назад» (используется только в автономном режиме).
 * @param showTopBar Рисовать ли собственную верхнюю панель экрана.
 * @param viewModel [SettingsViewModel] для управления состоянием экрана.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAuth: () -> Unit,
    onBack: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    /** Локальное состояние текстового поля ввода URL. */
    var urlInput by remember { mutableStateOf("") }

    // Определяем, есть ли уже сохранённый URL (для показа кнопки «Назад»)
    val hasSavedUrl = (uiState as? SettingsUiState.Success)?.currentUrl?.isNotBlank() == true

    // Синхронизируем локальное поле ввода с состоянием из ViewModel
    LaunchedEffect(uiState) {
        if (uiState is SettingsUiState.Success) {
            urlInput = (uiState as SettingsUiState.Success).currentUrl
        }
    }

    // Обработка событий навигации
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    /**
     * «Тело» экрана настроек без верхнего бара.
     * Вынесено в локальный composable-лямбду, чтобы один и тот же контент
     * переиспользовался в автономном и встроенном режимах.
     */
    val content: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.Md),
            verticalArrangement = Arrangement.spacedBy(Spacing.Md)
        ) {
            Text(
                text = if (!hasSavedUrl) {
                    "Для входа в систему необходимо указать адрес API-сервера. Изменения вступят в силу немедленно для всех новых запросов."
                } else {
                    "Укажите адрес API-сервера. Изменения вступят в силу немедленно для всех новых запросов."
                },
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
                enabled = uiState !is SettingsUiState.Saving && uiState !is SettingsUiState.Testing
            )

            // Индикатор статуса подключения
            when (connectionStatus) {
                is ServerConnectionStatus.NotChecked -> {
                    // Не отображаем ничего, если проверка не выполнялась
                }
                is ServerConnectionStatus.Checking -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ComponentSize.ProgressSm),
                            strokeWidth = ComponentSize.ProgressStrokeThin
                        )
                        Text(
                            text = "Проверка подключения...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is ServerConnectionStatus.Connected -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Сервер доступен",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(IconSize.Xs)
                        )
                        Text(
                            text = "Сервер доступен",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is ServerConnectionStatus.Disconnected -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Сервер недоступен",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(IconSize.Xs)
                        )
                        Text(
                            text = "Сервер недоступен: ${(connectionStatus as ServerConnectionStatus.Disconnected).error}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Отображение ошибок валидации и сохранения
            if (uiState is SettingsUiState.Error) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
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
                horizontalArrangement = Arrangement.spacedBy(Spacing.Md)
            ) {
                // Кнопка проверки подключения
                OutlinedButton(
                    onClick = { viewModel.testConnection(urlInput) },
                    modifier = Modifier.weight(1f),
                    enabled = urlInput.isNotBlank() && uiState !is SettingsUiState.Saving && uiState !is SettingsUiState.Testing
                ) {
                    Icon(
                        Icons.Default.Wifi,
                        contentDescription = "Проверить подключение",
                        modifier = Modifier.size(IconSize.Sm)
                    )
                    Spacer(modifier = Modifier.width(Spacing.Sm))
                    Text("Проверить")
                }

                // Кнопка сброса
                OutlinedButton(
                    onClick = { viewModel.resetToDefault() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is SettingsUiState.Saving && uiState !is SettingsUiState.Testing
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.Sm)
                    )
                    Spacer(modifier = Modifier.width(Spacing.Sm))
                    Text("По умолчанию")
                }
            }

            // Кнопка сохранения
            Button(
                onClick = { viewModel.saveUrl(urlInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = urlInput.isNotBlank() && uiState !is SettingsUiState.Saving && uiState !is SettingsUiState.Testing
            ) {
                if (uiState is SettingsUiState.Saving) {
                    // Компактный индикатор внутри кнопки — размер иконки кнопки.
                    CircularProgressIndicator(
                        modifier = Modifier.size(IconSize.Sm),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = ComponentSize.ProgressStrokeThin
                    )
                    Spacer(modifier = Modifier.width(Spacing.Sm))
                    Text("Сохранение...")
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.Sm)
                    )
                    Spacer(modifier = Modifier.width(Spacing.Sm))
                    Text(if (!hasSavedUrl) "Сохранить и продолжить" else "Сохранить")
                }
            }
        }
    }

    if (showTopBar) {
        // Автономный режим: экран сам предоставляет верхнюю панель
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Настройки сервера") },
                    navigationIcon = {
                        if (hasSavedUrl) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding -> content(innerPadding) }
    } else {
        // Встроенный режим: верхняя панель уже предоставлена MainShell
        content(PaddingValues(0.dp))
    }
}