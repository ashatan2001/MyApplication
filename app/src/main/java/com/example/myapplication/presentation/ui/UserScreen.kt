package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.UserModel
import com.example.myapplication.presentation.user.UserUiState
import com.example.myapplication.presentation.user.UserViewModel

@Composable
fun UserScreen(
    userId: Int,
    viewModel: UserViewModel = hiltViewModel(),
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    when (val state = userState) {
        is UserUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UserUiState.Success -> {
            UserContent(user = state.user)
        }
        is UserUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun UserContent(user: UserModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "ID: ${user.id}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "FIO: ${user.fullname}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Position: ${user.position}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "PositionId: ${user.positionId}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "isEmployee: ${user.isEmployee}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "isActive: ${user.isActive}", style = MaterialTheme.typography.bodyMedium)
    }
}