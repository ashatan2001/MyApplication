package com.example.domain.util

import com.example.domain.exception.AppException

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: AppException) : Result<Nothing>()
}