package com.example.data.exception

import com.example.domain.exception.AppException

/**
 * Базовое исключение слоя данных.
 *
 * Наследуется от [AppException], но добавляет семантику уровня:
 * все исключения из этого файла должны перехватываться в репозиториях
 * и маппиться в доменные исключения ([com.example.domain.exception.DomainException]).
 *
 * Диапазоны кодов:
 * - 1000–1099 — инфраструктурные ошибки (сеть, парсинг, кэш)
 * - 401, 419 — ошибки авторизации (дублируют HTTP-статусы)
 */

open class DataLayerException(
    errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : AppException(errorCode, message, cause)

/** Ошибка сети: таймаут, DNS, отсутствие соединения. */
class NetworkException(
    message: String = "Network error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1001, message = message, cause = cause)

/** Ошибка сервера. [httpCode] содержит HTTP-статус ответа (5xx, 4xx). */
class ServerException(
    val httpCode: Int,
    message: String = "Server error",
    cause: Throwable? = null
) : DataLayerException(errorCode = httpCode, message = message, cause = cause)

/**
 * Бизнес-ошибка от API.
 *
 * @param errorNumber Код ошибки из тела ответа сервера (поле `errorNumber`).
 *                    Отличается от [errorCode], который наследуется как `errorNumber`.
 */
class ApiException(
    val errorNumber: Int,
    message: String,
    cause: Throwable? = null
) : DataLayerException(errorCode = errorNumber, message = message, cause = cause)

/** Ошибка сериализации/десериализации данных. */
class DataParsingException(
    message: String = "Data parsing error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1002, message = message, cause = cause)


/** Ошибка работы с локальным кэшем / DataStore. */
class CacheException(
    message: String = "Cache error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1003, message = message, cause = cause)

/** HTTP 401/403. Сессия отсутствует или токен недействителен. */
class UnauthorizedException(
    message: String = "Unauthorized or Forbidden",
    cause: Throwable? = null
) : DataLayerException(errorCode = 401, message = message, cause = cause)

/** Общее исключение для ошибок доступа к данным без конкретной категории. */
class DataException(
    message: String = "Data error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1004, message = message, cause = cause)

/**
 * HTTP 419. Сессия истекла на сервере.
 * Перехватывается [com.example.data.network.TokenInterceptor] для автообновления токена.
 */
class SessionExpiredException(
    message: String = "Сессия истекла. Выполняется перенаправление на экран входа.",
    cause: Throwable? = null
) : DataLayerException(message = message, cause = cause)