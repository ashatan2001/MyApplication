package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.example.domain.model.Person
import com.example.myapplication.presentation.viewmodel.PersonUiState
import com.example.myapplication.presentation.viewmodel.PersonViewModel

/**
 * Экран профиля пользователя.
 *
 * Верхняя панель с кнопкой "Назад" предоставляется глобальной оболочкой [MainShell],
 * поэтому собственный [Scaffold] и [TopAppBar] здесь удалены.
 *
 * @param onBack Колбэк для возврата на предыдущий экран (обрабатывается [MainShell]).
 * @param viewModel [PersonViewModel] для загрузки данных пользователя.
 */
@Composable
fun PersonScreen(
    onBack: () -> Unit,
    viewModel: PersonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Загрузка данных при первом входе на экран
    LaunchedEffect(Unit) {
        viewModel.loadPersonData()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is PersonUiState.Loading -> {
                CircularProgressIndicator()
            }
            is PersonUiState.Success -> {
                PersonContent(person = state.person)
            }
            is PersonUiState.Error -> {
                Column(
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
                    Button(onClick = { viewModel.loadPersonData() }) {
                        Text("Повторить")
                    }
                }
            }
        }
    }
}

/**
 * Основной контент экрана профиля.
 *
 * @param person Модель данных пользователя для отображения.
 */
@Composable
private fun PersonContent(person: Person) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = person.fullName,
            style = MaterialTheme.typography.headlineMedium
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow("ID", person.id)
                InfoRow("Должность", person.position ?: "Не указана")
                InfoRow("ID должности", person.positionId?.toString() ?: "Не указан")
                InfoRow("Сотрудник", if (person.isEmployee == true) "Да" else "Нет")
                InfoRow("Активен", if (person.isActive == true) "Да" else "Нет")
            }
        }
    }
}

/**
 * Строка "ключ-значение" для отображения информации о пользователе.
 *
 * @param label Название поля.
 * @param value Значение поля.
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}