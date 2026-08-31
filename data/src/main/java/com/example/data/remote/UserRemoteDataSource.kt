package com.example.data.remote

import com.example.data.dto.UserDto
import com.example.domain.exception.NetworkException
import com.example.domain.exception.DataException
import com.example.domain.exception.UserNotFoundException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val api: UserApi
) {
    suspend fun getUser(id: Int): UserDto {
        return try {
            api.getUser(id)
        } catch (e: IOException) {
            throw NetworkException("Network error: ${e.message}", e)
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw UserNotFoundException("User $id not found", e)
                401, 403 -> throw NetworkException("Unauthorized", e)
                else -> throw DataException("Server error: ${e.code()}", e)
            }
        } catch (e: Exception) {
            throw DataException("Unexpected error: ${e.message}", e)
        }
    }
}