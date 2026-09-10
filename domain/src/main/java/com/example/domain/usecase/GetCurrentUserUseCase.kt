package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.model.UserModel
import com.example.domain.repository.AuthRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): CustomResult<UserModel> {
        return try {
            val user = repository.getCurrentUser()
            CustomResult.Success(user)
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(AppException(message = "Failed to get current user", cause = e))
        }
    }
}