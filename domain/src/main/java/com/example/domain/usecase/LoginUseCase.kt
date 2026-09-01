package com.example.domain.usecase

import com.example.domain.model.AuthSuccess
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(login: String, password: String): AuthSuccess {
        return repository.login(login, password)
    }
}