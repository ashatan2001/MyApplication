package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.util.ComponentSize
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.LayoutSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.viewmodel.AuthEvent
import com.example.myapplication.presentation.viewmodel.AuthUiState
import com.example.myapplication.presentation.viewmodel.AuthViewModel

/**
 * Экран авторизации с формой логина и пароля.
 *
 * Ширина формы адаптивная: на телефонах поля занимают всю ширину экрана,
 * на планшетах и в ландшафтной ориентации форма центрируется и ограничивается
 * значением [LayoutSize.FormMaxWidth]. При открытой клавиатуре или на низких экранах
 * форма прокручивается вертикально.
 *
 * @param onNavigateToHome Вызывается после успешного входа для перехода на главный экран.
 * @param onNavigateToSettings Вызывается для перехода на экран настроек сервера.
 * @param viewModel [AuthViewModel] для управления состоянием и обработки событий.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    /**
     * Собираем одноразовые события (навигация) через Channel.
     * LaunchedEffect(Unit) гарантирует, что событие обработается один раз,
     * даже при пересоздании композиции.
     */
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AuthEvent.LoginSuccess) {
                onNavigateToHome()
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = LayoutSize.FormMaxWidth) // ограничение ширины для планшета
                    .fillMaxWidth()                         // на телефоне — вся ширина экрана
                    .padding(horizontal = Spacing.Lg, vertical = Spacing.Xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Авторизация",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = Spacing.Xl)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Логин") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Иконка пользователя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is AuthUiState.Loading,
                    isError = uiState is AuthUiState.Error
                )
                Spacer(Modifier.height(Spacing.Md))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is AuthUiState.Loading,
                    isError = uiState is AuthUiState.Error
                )
                Spacer(Modifier.height(Spacing.Lg))
                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ComponentSize.ButtonHeight),
                    enabled = uiState !is AuthUiState.Loading &&
                            username.isNotBlank() && password.isNotBlank()
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ComponentSize.ProgressMd),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Войти")
                    }
                }
                if (uiState is AuthUiState.Error) {
                    Spacer(Modifier.height(Spacing.Md))
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(Spacing.Lg))

                // Кнопка для перехода к настройкам сервера
                TextButton(
                    onClick = onNavigateToSettings,
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.Sm)
                    )
                    Spacer(Modifier.width(Spacing.Sm))
                    Text("Изменить сервер")
                }
            }
        }
    }
}