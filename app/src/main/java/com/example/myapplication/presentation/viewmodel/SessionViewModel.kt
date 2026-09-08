package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.network.AuthEventBus
import com.example.domain.model.AuthState
import com.example.domain.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    val authState: StateFlow<AuthState> = observeAuthStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AuthState.Loading
        )

    val logoutEvents: SharedFlow<Unit> = authEventBus.logoutEvents
}