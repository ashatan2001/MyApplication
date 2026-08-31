package com.example.domain.usecase

import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import javax.inject.Inject

// Добавить обработку ошибок и юнит-тесты с моками репозитория
class UserUseCase @Inject constructor (
    private val repository: UserRepository,
) {
    suspend operator fun invoke(userId: Int): UserModel {
        return repository.getUser(userId)
    }
}