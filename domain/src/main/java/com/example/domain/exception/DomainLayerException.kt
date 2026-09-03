package com.example.domain.exception

// Базовое исключение приложения (доступно всем слоям)
open class AppException(
    val errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// Исключения бизнес-логики (домена)
sealed class DomainException(
    errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : AppException(errorCode, message, cause)

class AuthenticationException(
    message: String = "Authentication failed",
    cause: Throwable? = null
) : DomainException(errorCode = 2001, message = message, cause = cause)

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