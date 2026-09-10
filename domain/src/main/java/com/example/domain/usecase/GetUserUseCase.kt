package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int): CustomResult<UserModel> {
        return try {
            require(userId > 0) { "User ID must be positive" }
            CustomResult.Success(repository.getUser(userId))
        } catch (e: UserNotFoundException) {
            CustomResult.Error(e)
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(AppException(message = "Failed to get user", cause = e))
        }
    }
}