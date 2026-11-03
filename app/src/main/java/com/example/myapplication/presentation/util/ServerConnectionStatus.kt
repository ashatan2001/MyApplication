package com.example.myapplication.presentation.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Статус подключения к серверу.
 */
sealed class ServerConnectionStatus {
    /** Подключение не проверялось */
    data object NotChecked : ServerConnectionStatus()

    /** Проверка выполняется */
    data object Checking : ServerConnectionStatus()

    /** Сервер доступен */
    data object Connected : ServerConnectionStatus()

    /** Сервер недоступен */
    data class Disconnected(
        val error: String,
    ) : ServerConnectionStatus()
}

/**
 * Компонент отображения статуса подключения к серверу.
 *
 * @param status Текущий статус подключения.
 */
@Composable
private fun ConnectionStatusIndicator(status: ServerConnectionStatus) {
    when (status) {
        is ServerConnectionStatus.NotChecked -> {
            // Не отображаем ничего, если проверка не выполнялась
        }

        is ServerConnectionStatus.Checking -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "Проверка подключения...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        is ServerConnectionStatus.Connected -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Сервер доступен",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Сервер доступен",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        is ServerConnectionStatus.Disconnected -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Сервер недоступен",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Сервер недоступен: ${status.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
