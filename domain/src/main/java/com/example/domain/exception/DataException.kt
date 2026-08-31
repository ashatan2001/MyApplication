package com.example.domain.exception

open class DataException(message: String, cause: Throwable? = null) : Exception(message, cause)

class UserNotFoundException(message: String, cause: Throwable? = null)
    : DataException(message, cause)

class DataParsingException(message: String, cause: Throwable? = null)
    : DataException(message, cause)

class NetworkException(message: String, cause: Throwable? = null)
    : DataException(message, cause)

class ServerException(message: String, cause: Throwable? = null)
    : DataException(message, cause)

class AuthenticationException(message: String, cause: Throwable? = null)
    : DataException(message, cause)

class CacheException(message: String, cause: Throwable? = null)
    : DataException(message, cause)