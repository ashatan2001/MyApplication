package com.example.app.presentation.user

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
                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun UserContent(user: UserModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "ID: ${user.id}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Login: ${user.login}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Name: ${user.fio}", style = MaterialTheme.typography.bodyMedium)
    }
}
