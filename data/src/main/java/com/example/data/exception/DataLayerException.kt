package com.example.data.exception

import com.example.domain.exception.AppException

open class DataLayerException(
    errorCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : AppException(errorCode, message, cause)

class NetworkException(
    message: String = "Network error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1001, message = message, cause = cause)

class ServerException(
    val httpCode: Int,
    message: String = "Server error",
    cause: Throwable? = null
) : DataLayerException(errorCode = httpCode, message = message, cause = cause)

class ApiException(
    val errorNumber: Int,
    message: String,
    cause: Throwable? = null
) : DataLayerException(errorCode = errorNumber, message = message, cause = cause)

class DataParsingException(
    message: String = "Data parsing error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1002, message = message, cause = cause)

class CacheException(
    message: String = "Cache error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1003, message = message, cause = cause)

class UnauthorizedException(
    message: String = "Unauthorized or Forbidden",
    cause: Throwable? = null
) : DataLayerException(errorCode = 401, message = message, cause = cause)

class DataException(
    message: String = "Data error",
    cause: Throwable? = null
) : DataLayerException(errorCode = 1004, message = message, cause = cause)