package com.example.data.util

import com.example.data.dto.ErrorResponseDto
import com.example.data.exception.*
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

/**
 * Конфигурация JSON для парсинга тел ошибок.
 * [coerceInputValues] позволяет обрабатывать некорректные значения
 * (например, число вместо строки) без падения.
 */
private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/**
 * Безопасная обёртка для выполнения сетевых запросов.
 *
 * Централизует обработку ошибок: парсит тело ошибки сервера в [ErrorResponseDto]
 * и преобразует в соответствующее исключение дата-слоя.
 * Позволяет не дублировать обработку ошибок в каждом DataSource.
 *
 * @param block Лямбда, выполняющая запрос и возвращающая [Response].
 * @return Десериализованное тело ответа при успехе.
 * @throws NetworkException при ошибке соединения.
 * @throws ApiException при бизнес-ошибке от сервера.
 * @throws UnauthorizedException при 401/403/419.
 * @throws DataParsingException при ошибке парсинга ответа.
 */
suspend fun <T> safeApiCall(block: suspend () -> Response<T>): T {
    android.util.Log.d("LOGIN_DEBUG", "[SafeApiCall] Начало выполнения запроса...")
    return try {
        val response = block()
        android.util.Log.d("LOGIN_DEBUG", "[SafeApiCall] Запрос завершен. Успех: ${response.isSuccessful}, Код: ${response.code()}")

        if (response.isSuccessful) {
            response.body() ?: run {
                // Body может быть null для ответов 204 No Content.
                // Приводим к Unit для совместимости с общим типом возврата.
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

/**
 * Парсит тело ошибки сервера в исключение дата-слоя.
 *
 * Приоритет:
 * 1. Пытается распарсить [ErrorResponseDto] из тела ответа.
 * 2. Если парсинг не удался — маппит по HTTP-коду.
 *
 * @param body Тело ошибки в формате JSON или текст.
 * @param httpCode HTTP-статус ответа.
 * @return Соответствующее исключение [DataLayerException].
 */
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

/**
 * Маппит HTTP-код ошибки в соответствующее исключение дата-слоя.
 * Используется когда тело ответа пустое или не содержит структурированной ошибки.
 *
 * @param code HTTP-статус ответа.
 * @param cause Исходное исключение (опционально).
 */fun mapHttpError(code: Int, cause: Throwable? = null): DataLayerException = when (code) {
    401 -> UnauthorizedException(message = "Сессия истекла. Войдите снова.", cause = cause)
    403 -> UnauthorizedException(message = "Доступ запрещен", cause = cause)
    419 -> UnauthorizedException(message = "Сессия истекла. Войдите снова.", cause = cause)
    404 -> DataLayerException(message = "Ресурс не найден", cause = cause)
    in 500..599 -> ServerException(httpCode = code, message = "Ошибка сервера: $code", cause = cause)
    else -> DataLayerException(message = "HTTP ошибка: $code", cause = cause)
}