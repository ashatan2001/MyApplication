package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.AuthenticationException
import com.example.domain.model.UserModel
import com.example.domain.repository.AuthRepository
import com.example.domain.util.Result
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<UserModel> {
        return try {
            val user = repository.getCurrentUser()
            Result.Success(user)
        } catch (e: AuthenticationException) {
            Result.Failure(e)
        } catch (e: AppException) {
            Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(AppException(message = "Failed to get current user", cause = e))
        }
    }
}