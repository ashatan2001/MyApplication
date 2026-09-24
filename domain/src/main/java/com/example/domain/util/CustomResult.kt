package com.example.domain.util

import com.example.domain.exception.AppException

/**
 * Обертка для результата выполнения операций, инкапсулирующая успешный ответ или ошибку.
 * Позволяет явно обрабатывать ошибки бизнес-логики без использования исключений в UI слое.
 *
 * @param T Тип данных при успешном выполнении.
 */
sealed class CustomResult<out T> {
    data class Success<T>(val data: T) : CustomResult<T>()
    data class Error(val exception: AppException) : CustomResult<Nothing>()
}