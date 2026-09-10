package com.example.data.util

import com.example.data.dto.ErrorResponseDto
import com.example.data.exception.*
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

private val json = Json {ignoreUnknownKeys = true}

suspend fun <T> safeApiCall(block: suspend () -> Response<T>): T {
    android.util.Log.d("LOGIN_DEBUG", "[SafeApiCall] Начало выполнения запроса...")
    return try {
        val response = block()
        android.util.Log.d("LOGIN_DEBUG", "[SafeApiCall] Запрос завершен. Успех: ${response.isSuccessful}, Код: ${response.code()}")
        if (response.isSuccessful) {
            response.body() ?: throw DataParsingException("Empty response body")
        } else {
            throw parseErrorBody(response.errorBody()?.string(), response.code())
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: HttpException) {
        throw parseErrorBody(e.response()?.errorBody()?.string(), e.code())
    } catch (e: IOException) {
        android.util.Log.e("LOGIN_DEBUG", "[SafeApiCall] Ошибка сети (IO)", e)
        throw NetworkException(cause = e)
    } catch (e: Exception) {
        android.util.Log.e("LOGIN_DEBUG", "[SafeApiCall] Неожиданная ошибка парсинга", e)
        throw DataParsingException(cause = e)
    }
}

private fun parseErrorBody(body: String?, httpCode: Int): DataLayerException {
    if (body.isNullOrBlank()) return mapHttpError(httpCode)
    return try {
        val dto = json.decodeFromString<ErrorResponseDto>(body)
        ApiException(errorNumber = dto.errorNumber, message = dto.message)
    } catch (_: Exception) {
        mapHttpError(httpCode)
    }
}

fun mapHttpError(code: Int, cause: Throwable? = null): DataLayerException = when (code) {
    401, 403 -> UnauthorizedException(cause = cause) // Важно: выбрасываем специфичное исключение
    404 -> DataLayerException(message = "Resource not found", cause = cause)
    in 500..599 -> ServerException(httpCode = code, cause = cause)
    else -> DataLayerException(message = "HTTP error: $code", cause = cause)
}