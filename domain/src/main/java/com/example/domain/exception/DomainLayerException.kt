// domain/src/main/java/com/example/domain/exception/DomainLayerException.kt
package com.example.domain.exception

open class AppException(
    val errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

sealed class DomainException(
    errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : AppException(errorCode, message, cause)

class UserNotFoundException(
    val userId: Int? = null,
    message: String = "User not found",
    cause: Throwable? = null
) : DomainException(errorCode = 2002, message = message, cause = cause)

class ValidationException(
    val field: String? = null,
    message: String = "Validation error",
    cause: Throwable? = null
) : DomainException(errorCode = 2003, message = message, cause = cause)

class NetworkConnectionException(
    message: String = "Нет подключения к интернету",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

class ServerUnavailableException(
    message: String = "Ошибка сервера",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

class AuthenticationFailedException(
    message: String = "Неверные учетные данные",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

class SessionExpiredDomainException(
    message: String = "Сессия истекла",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

class ServerApiException(
    val errorNumber: Int,
    message: String,
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)