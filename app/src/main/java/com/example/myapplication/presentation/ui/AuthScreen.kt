package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.viewmodel.AuthEvent
import com.example.myapplication.presentation.viewmodel.AuthUiState
import com.example.myapplication.presentation.viewmodel.AuthViewModel

/**
 * Максимальная ширина формы авторизации.
 *
 * На телефонах (экран уже этого значения) форма занимает всю доступную ширину.
 * На планшетах и широких экранах форма ограничивается этой шириной
 * и центрируется, чтобы поля не растягивались на весь дисплей.
 */
private val AuthFormMaxWidth = 480.dp

/**
 * Экран авторизации с формой логина и пароля.
 *
 * Ширина формы адаптивная: на телефонах поля занимают всю ширину экрана,
 * на планшетах и в ландшафтной ориентации форма центрируется и ограничивается
 * значением [AuthFormMaxWidth]. При открытой клавиатуре или на низких экранах
 * форма прокручивается вертикально.
 *
 * @param onNavigateToHome Вызывается после успешного входа для перехода на главный экран.
 * @param viewModel [AuthViewModel] для управления состоянием и обработки событий.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
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
                    .widthIn(max = AuthFormMaxWidth) // ограничение ширины для планшета
                    .fillMaxWidth()                   // на телефоне — вся ширина экрана
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Авторизация",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
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
                Spacer(Modifier.height(16.dp))
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
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = uiState !is AuthUiState.Loading &&
                            username.isNotBlank() && password.isNotBlank()
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Войти")
                    }
                }
                if (uiState is AuthUiState.Error) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}