package com.example.app.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.viewmodel.AuthUiState
import com.example.myapplication.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Состояние полей ввода
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Обработка состояний
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                val success = uiState as AuthUiState.Success
                // Можно показать Snackbar с сообщением
                onLoginSuccess()
                viewModel.resetState()
            }
            else -> {}
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
            // Заголовок
            Text(
                text = "Авторизация",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Поле логина
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Логин") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Логин")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле пароля
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Пароль")
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.Lock
                            } else {
                                Icons.Default.Lock
                            },
                            contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка входа
            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading &&
                        username.isNotBlank() &&
                        password.isNotBlank()
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

            // Отображение ошибки
            if (uiState is AuthUiState.Error) {
                val error = uiState as AuthUiState.Error
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}