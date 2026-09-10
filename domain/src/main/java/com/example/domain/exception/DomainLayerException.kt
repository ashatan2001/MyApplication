package com.example.domain.exception

// Базовое исключение приложения (доступно всем слоям)
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