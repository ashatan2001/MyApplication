package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class LoginUseCase (
    private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(refreshToken: String): String {
            return repository.login()
        }
}