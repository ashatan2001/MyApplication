package com.example.domain.exception

/**
 * Базовое исключение доменного слоя.
 *
 * Все исключения приложения наследуются от этого класса,
 * что позволяет перехватывать их единым блоком в UseCase/ViewModel.
 *
 * @param errorCode Внутренний код ошибки для логирования и аналитики.
 * @param message Человекочитаемое сообщение об ошибке.
 * @param cause Исходное исключение, вызвавшее данное.
 */
open class AppException(
    val errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * sealed-иерархия доменных исключений.
 *
 * Позволяет использовать исчерпывающий `when` для обработки
 * конкретных бизнес-ошибок без `else`-ветки.
 */
sealed class DomainException(
    errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : AppException(errorCode, message, cause)

/**
 * Пользователь не найден в системе.
 *
 * @param userId ID запрашиваемого пользователя (для логирования).
 */
class UserNotFoundException(
    val userId: Int? = null,
    message: String = "User not found",
    cause: Throwable? = null
) : DomainException(errorCode = 2002, message = message, cause = cause)

/**
 * Ошибка валидации пользовательского ввода.
 *
 * @param field Название поля, не прошедшего валидацию (например, "login", "password").
 */
class ValidationException(
    val field: String? = null,
    message: String = "Validation error",
    cause: Throwable? = null
) : DomainException(errorCode = 2003, message = message, cause = cause)

/** Отсутствует подключение к интернету. */
class NetworkConnectionException(
    message: String = "Нет подключения к интернету",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

/** Сервер недоступен (таймаут, 5xx). */
class ServerUnavailableException(
    message: String = "Ошибка сервера",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

/** Неверный логин или пароль. */
class AuthenticationFailedException(
    message: String = "Неверные учетные данные",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

/** Сессия истекла, требуется повторная авторизация. */
class SessionExpiredDomainException(
    message: String = "Сессия истекла",
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)

/**
 * Ошибка, возвращённая API сервера.
 *
 * @param errorNumber Код ошибки из тела ответа сервера (отличается от [errorCode]).
 */
class ServerApiException(
    val errorNumber: Int,
    message: String,
    cause: Throwable? = null
) : DomainException(message = message, cause = cause)