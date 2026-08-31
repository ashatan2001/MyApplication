package com.example.domain.usecase

import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class CheckAuthStateUseCase {
    private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(): Flow<AuthState> {
            return repository.getAuthState()
        }
}