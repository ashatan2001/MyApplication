package com.example.data.util

import com.example.data.dto.ErrorResponseDto
import com.example.data.exception.*
import com.example.domain.exception.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/**
 * Использовать, когда успешный ответ обязан содержать тело.
 */
suspend fun <T> safeApiCall(
    emptyValue: T? = null,
    block: suspend () -> Response<T>
): T {
    return try {
        val response = block()

        if (response.isSuccessful) {
            // 204 No Content — допустимый ответ для пустых архивов
            if (response.code() == 204) {
                emptyValue ?: throw DataLayerException(
                    message = "Пустой ответ (204)",
                    errorCode = 204
                )
            }

            response.body()
                ?: emptyValue
                ?: throw DataParsingException(message = "Пустое тело ответа")
        } else {
            throw parseErrorBody(response.errorBody()?.string(), response.code())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: DataLayerException) {
        throw e
    } catch (e: IOException) {
        throw NetworkException(cause = e)
    } catch (e: Exception) {
        throw DataParsingException(message = e.message ?: "Неизвестная ошибка", cause = e)
    }
}

private fun parseErrorBody(body: String?, httpCode: Int): DataLayerException {
    if (body.isNullOrBlank()) {
        return mapHttpError(httpCode)
    }

    return try {
        val dto = json.decodeFromString<ErrorResponseDto>(body)
        val errorNum = dto.errorNumber ?: 0
        val errorMessage = dto.message ?: "Ошибка сервера (код $errorNum)"

        when (httpCode) {
            401, 403 -> UnauthorizedException(message = errorMessage)
            419 -> SessionExpiredException(message = errorMessage)
            else -> ApiException(errorNumber = errorNum, message = errorMessage)
        }
    } catch (e: Exception) {
        when (httpCode) {
            401, 403 -> UnauthorizedException(message = body, cause = e)
            419 -> SessionExpiredException(message = body, cause = e)
            else -> DataLayerException(message = body, errorCode = httpCode, cause = e)
        }
    }
}

fun mapHttpError(code: Int, cause: Throwable? = null): DataLayerException = when (code) {
    401 -> UnauthorizedException(message = "Сессия истекла. Войдите снова.", cause = cause)
    403 -> UnauthorizedException(message = "Доступ запрещен", cause = cause)
    419 -> SessionExpiredException(message = "Сессия истекла. Войдите снова.", cause = cause)
    404 -> DataLayerException(message = "Ресурс не найден", errorCode = 404, cause = cause)
    in 500..599 -> ServerException(httpCode = code, message = "Ошибка сервера: $code", cause = cause)
    else -> DataLayerException(message = "HTTP ошибка: $code", errorCode = code, cause = cause)
}

/**
 * Универсальная обёртка для вызовов в репозиториях.
 * Централизует маппинг исключений дата-слоя в доменные исключения.
 */
suspend fun <T> safeRepositoryCall(
    contextMessage: String = "Неизвестная ошибка",
    onAuthError: suspend () -> Unit = {},
    notFoundMapper: ((DataLayerException) -> AppException)? = null,
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: UnauthorizedException) {
        onAuthError()
        throw AuthenticationFailedException(message = e.message ?: "Ошибка авторизации", cause = e)
    } catch (e: SessionExpiredException) {
        onAuthError()
        throw SessionExpiredDomainException(message = e.message ?: "Сессия истекла", cause = e)
    } catch (e: NetworkException) {
        throw NetworkConnectionException(message = e.message ?: "Нет подключения к интернету", cause = e)
    } catch (e: ServerException) {
        throw ServerUnavailableException(message = e.message ?: "Ошибка сервера", cause = e)
    } catch (e: ApiException) {
        throw ServerApiException(errorNumber = e.errorNumber, message = e.message ?: "Ошибка API", cause = e)
    } catch (e: DataLayerException) {
        if (e.errorCode == 404 && notFoundMapper != null) {
            throw notFoundMapper(e)
        }
        throw AppException(message = "$contextMessage (код: ${e.errorCode})", cause = e)
    } catch (e: AppException) {
        throw e
    } catch (e: Exception) {
        throw AppException(message = "$contextMessage: ${e.message ?: "неизвестная ошибка"}", cause = e)
    }
}