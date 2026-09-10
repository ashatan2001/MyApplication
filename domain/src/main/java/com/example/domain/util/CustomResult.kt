package com.example.domain.util

import com.example.domain.exception.AppException

sealed class CustomResult<out T> {
    data class Success<T>(val data: T) : CustomResult<T>()
    data class Error(val exception: AppException) : CustomResult<Nothing>()
}