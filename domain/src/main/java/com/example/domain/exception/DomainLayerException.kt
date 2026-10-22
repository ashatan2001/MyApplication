package com.example.domain.exception

/**
 * Базовое исключение приложения.
 */
open class AppException(
    val errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * sealed-иерархия доменных исключений.
 */
sealed class DomainException(
    errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : AppException(errorCode, message, cause)

class PersonNotFoundException(
    val personId: Int? = null,
    message: String = "Пользователь не найден",
    cause: Throwable? = null
) : DomainException(errorCode = 2002, message = message, cause = cause)

class DstPointNotFoundException(
    val zone: Int? = null,
    message: String = "Точка выгрузки бетона не найдена",
    cause: Throwable? = null
) : DomainException(errorCode = 2002, message = message, cause = cause)

class LoadNotFoundException(
    message: String = "Заявки на отгрузку не найдены",
    cause: Throwable? = null
) : DomainException(errorCode = 2002, message = message, cause = cause)

class ValidationException(
    val field: String? = null,
    message: String = "Ошибка валидации",
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
    message: String = "Ошибка авторизации",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

class SessionExpiredDomainException(
    message: String = "Сессия истекла",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

class ServerApiException(
    message: String,
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)