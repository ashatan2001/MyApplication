package com.example.data.remote

import android.util.Log
import com.example.data.dto.LoginRequestDto
import com.example.data.dto.LoginResponseDto
import com.example.data.dto.UserDto
import com.example.domain.exception.AuthenticationException
import com.example.domain.exception.DataException
import com.example.domain.exception.DataParsingException
import com.example.domain.exception.NetworkException
import com.example.domain.exception.ServerException
import java.io.IOException
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi
) {
    suspend fun login(login: String, password: String): LoginResponseDto {
        return try {
            val requestDto = LoginRequestDto(username = login, password = password)
            val response = api.login(requestDto)

            if (response.isSuccessful) {
                response.body() ?: throw DataParsingException("Пустой ответ от сервера при успешной авторизации")
            } else {
                when (response.code()) {
                    401, 403 -> throw AuthenticationException("Неверный логин или пароль", null)
                    400 -> throw DataException("Ошибка валидации данных (например, слишком короткий пароль)")
                    in 500..599 -> throw ServerException("Внутренняя ошибка сервера: ${response.code()}")
                    else -> throw DataException("Неизвестная ошибка сервера: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            throw NetworkException("Отсутствует подключение к сети или таймаут", e)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: DataParsingException) {
            throw e
        } catch (e: Exception) {
            throw DataException("Непредвиденная ошибка при авторизации", e)
        }
    }

    suspend fun refreshToken(): Unit {
        try {
            val response = api.refreshToken()
            if (!response.isSuccessful) {
                throw AuthenticationException("Сессия истекла требуется повторный вход", null)
            }
        } catch (e: IOException) {
            throw NetworkException("Ошибка сети при обновлении токена", e)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            throw DataException("Непредвиденная ошибка при обновлении токена", e)
        }
    }

    suspend fun logout() {
        try {
            val response = api.logout()
            if (!response.isSuccessful) {
                Log.w("AuthRemoteDataSource", "Logout failed: ${response.code()}")
            }
        } catch (e: IOException) {
            // Игнорируем ошибку сети
        } catch (e: Exception) {
            // Игнорируем прочие ошибки, чтобы не блокировать выход из приложения
        }
    }

    suspend fun getCurrentUser(): UserDto {
        return try {
            val response = api.getCurrentUser()
            if (response.isSuccessful) {
                response.body() ?: throw DataParsingException("Пустой ответ от сервера")
            } else {
                when (response.code()) {
                    401, 403 -> throw AuthenticationException("Токен недействителен", null)
                    in 500..599 -> throw ServerException("Внутренняя ошибка сервера: ${response.code()}")
                    else -> throw DataException("Ошибка получения профиля: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            throw NetworkException("Ошибка сети при получении профиля", e)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            throw DataException("Непредвиденная ошибка при получении профиля", e)
        }
    }
}