package com.example.data.util

import com.example.data.exception.*
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> Response<T>): T {
    return try {
        val response = block()
        if (response.isSuccessful) {
            response.body() ?: throw DataParsingException("Empty response body")
        } else {
            throw mapHttpError(response.code())
        }
    } catch (e: HttpException) {
        throw mapHttpError(e.code(), e)
    } catch (e: IOException) {
        throw NetworkException(cause = e)
    }
}

fun mapHttpError(code: Int, cause: Throwable? = null): DataLayerException = when (code) {
    401, 403 -> DataLayerException(message = "Unauthorized", cause = cause)
    404 -> DataLayerException(message = "Resource not found", cause = cause)
    in 500..599 -> DataLayerException(message = "Server error: $code", cause = cause)
    // ИСПРАВЛЕНО: Явное указание имен параметров
    else -> DataLayerException(message = "HTTP error: $code", cause = cause)
}