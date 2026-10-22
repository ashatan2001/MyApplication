package com.example.data.util

import android.util.Log
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
            // 204 No Content — допустимый ответ для пустых данных
            if (response.code() == 204) {
                emptyValue ?: throw NoDataException()
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
        throw DataParsingException(
            message = e.message ?: "Не удалось обработать ответ",
            cause = e
        )
    }
}

/**
 * Универсальная обёртка для вызовов в репозиториях.
 * Централизует маппинг исключений дата-слоя в доменные исключения.
 */
suspend fun <T> safeRepositoryCall(
    contextMessage: String = "Не удалось выполнить запрос",
    onAuthError: suspend () -> Unit = {},
    notFoundMapper: ((DataLayerException) -> AppException)? = null,
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: UnauthorizedException) {
        onAuthError()
        throw AuthenticationFailedException(
            message = e.message.orFallback("Ошибка авторизации"),
            cause = e
        )
    } catch (e: SessionExpiredException) {
        onAuthError()
        throw SessionExpiredDomainException(
            message = e.message.orFallback("Сессия истекла"),
            cause = e
        )
    } catch (e: NetworkException) {
        throw NetworkConnectionException(
            message = e.message.orFallback("Нет подключения к интернету"),
            cause = e
        )
    } catch (e: ServerException) {
        throw ServerUnavailableException(
            message = e.message.orFallback("Ошибка сервера"),
            cause = e
        )
    } catch (e: DataParsingException) {
        throw AppException(
            message = e.message.orFallback("Не удалось обработать ответ сервера"),
            cause = e
        )
    } catch (e: ApiException) {
        throw ServerApiException(
            message = e.message.orFallback("Ошибка API"),
            cause = e
        )
    } catch (e: DataLayerException) {
        if (e.errorCode == 404 && notFoundMapper != null) {
            throw notFoundMapper(e)
        }

        val fallback = if (e.errorCode != null) {
            "$contextMessage (код: ${e.errorCode})"
        } else {
            contextMessage
        }

        throw AppException(
            message = e.message.orFallback(fallback),
            cause = e
        )
    } catch (e: AppException) {
        throw e
    } catch (e: Exception) {
        throw AppException(
            message = e.message.orFallback(contextMessage),
            cause = e
        )
    }
}

private fun String?.orFallback(fallback: String): String =
    this?.takeIf { it.isNotBlank() } ?: fallback

private fun parseErrorBody(body: String?, httpCode: Int): DataLayerException {
    if (body.isNullOrBlank()) {
        return mapHttpError(httpCode)
    }

    val errors = parseErrors(body)

    if (errors.isEmpty()) {
        Log.w(
            "SafeApiCall",
            "Не удалось распарсить тело ошибки ($httpCode): ${body.take(1000)}"
        )
        return mapHttpError(httpCode)
    }

    val message = errors
        .mapNotNull { it.message?.takeIf(String::isNotBlank) }
        .joinToString("\n")
        .ifBlank { fallbackMessageFor(httpCode) }

    return httpException(
        code = httpCode,
        message = message,
        errors = errors
    )
}

/**
 * Пытается распарсить и как массив, и как одиночный объект.
 */
private fun parseErrors(body: String): List<ErrorResponseDto> {
    runCatching { json.decodeFromString<List<ErrorResponseDto>>(body) }
        .getOrNull()
        ?.let { return it }

    return runCatching { listOf(json.decodeFromString<ErrorResponseDto>(body)) }
        .getOrDefault(emptyList())
}

/**
 * Единственная точка создания исключения по HTTP-коду.
 */
private fun httpException(
    code: Int,
    message: String,
    errors: List<ErrorResponseDto> = emptyList(),
    cause: Throwable? = null
): DataLayerException = when (code) {
    401, 403 -> UnauthorizedException(message = message, cause = cause)
    419 -> SessionExpiredException(message = message, cause = cause)
    404 -> DataLayerException(message = message, errorCode = 404, cause = cause)
    in 500..599 -> ServerException(httpCode = code, message = message, cause = cause)
    else -> ApiException(
        errorNumber = errors.firstOrNull()?.errorNumber ?: code,
        message = message,
        errors = errors,
        cause = cause
    )
}

fun mapHttpError(code: Int, cause: Throwable? = null): DataLayerException =
    httpException(
        code = code,
        message = fallbackMessageFor(code),
        cause = cause
    )

/**
 * Короткие запасные сообщения на случай,
 * если сервер не вернул понятный текст ошибки.
 */
private fun fallbackMessageFor(code: Int): String = when (code) {
    401, 403, 419 -> "Ошибка авторизации"
    404 -> "Данные не найдены"
    in 500..599 -> "Ошибка сервера"
    else -> "Ошибка запроса (код $code)"
}