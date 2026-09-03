package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.AuthenticationException
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import com.example.domain.util.Result
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int): Result<UserModel> {
        return try {
            require(userId > 0) { "User ID must be positive" }
            Result.Success(repository.getUser(userId))
        } catch (e: UserNotFoundException) {
            Result.Failure(e)
        } catch (e: AuthenticationException) {
            Result.Failure(e)
        } catch (e: AppException) {
            Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(AppException(message = "Failed to get user", cause = e))
        }
    }
}