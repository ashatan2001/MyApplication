package com.example.data.util

import com.example.data.dto.ErrorResponseDto
import com.example.data.exception.*
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

suspend fun <T> safeApiCall(block: suspend () -> Response<T>): T {
    android.util.Log.d("LOGIN_DEBUG", "[SafeApiCall] Начало выполнения запроса...")
    return try {
        val response = block()
        android.util.Log.d("LOGIN_DEBUG", "[SafeApiCall] Запрос завершен. Успех: ${response.isSuccessful}, Код: ${response.code()}")

        if (response.isSuccessful) {
            response.body() ?: run {
                @Suppress("UNCHECKED_CAST")
                Unit as T
            }
        } else {
            throw parseErrorBody(response.errorBody()?.string(), response.code())
        }
    } catch (e: SessionExpiredException) {
        throw e
    } catch (e: ApiException) {
        throw e
    } catch (e: UnauthorizedException) {
        throw e
    } catch (e: IOException) {
        android.util.Log.e("LOGIN_DEBUG", "[SafeApiCall] Ошибка сети (IO)", e)
        throw NetworkException(cause = e)
    } catch (e: Exception) {
        android.util.Log.e("LOGIN_DEBUG", "[SafeApiCall] Неожиданная ошибка", e)
        throw DataParsingException(message = e.message ?: "Неизвестная ошибка", cause = e)
    }
}

private fun parseErrorBody(body: String?, httpCode: Int): DataLayerException {
    android.util.Log.d("LOGIN_DEBUG", "[parseErrorBody] code=$httpCode, body=$body")

    if (body.isNullOrBlank()) {
        return mapHttpError(httpCode)
    }

    return try {
        val dto = json.decodeFromString<ErrorResponseDto>(body)
        android.util.Log.d("LOGIN_DEBUG", "[parseErrorBody] parsed dto=$dto")

        val errorNum = dto.errorNumber ?: 0
        val errorMessage = dto.message ?: "Ошибка сервера (код ${dto.errorNumber})"

        ApiException(errorNumber = errorNum, message = errorMessage)

    } catch (e: Exception) {
        android.util.Log.w("LOGIN_DEBUG", "[parseErrorBody] Не удалось распарсить JSON, используем raw body. Причина: ${e.message}")

        if (httpCode == 401 || httpCode == 403 || httpCode == 419) {
            UnauthorizedException(message = body, cause = e)
        } else {
            DataLayerException(message = body, cause = e)
        }
    }
}

fun mapHttpError(code: Int, cause: Throwable? = null): DataLayerException = when (code) {
    401 -> UnauthorizedException(message = "Сессия истекла. Войдите снова.", cause = cause)
    403 -> UnauthorizedException(message = "Доступ запрещен", cause = cause)
    419 -> UnauthorizedException(message = "Сессия истекла. Войдите снова.", cause = cause)
    404 -> DataLayerException(message = "Ресурс не найден", cause = cause)
    in 500..599 -> ServerException(httpCode = code, message = "Ошибка сервера: $code", cause = cause)
    else -> DataLayerException(message = "HTTP ошибка: $code", cause = cause)
}