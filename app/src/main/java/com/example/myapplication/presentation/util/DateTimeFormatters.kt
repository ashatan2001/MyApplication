package com.example.myapplication.presentation.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale

object DateTimeFormatters {
    const val PATTERN_DATE_TIME = "dd.MM.yyyy HH:mm:ss"
    const val PATTERN_DATE = "dd.MM.yyyy"
    const val PATTERN_TIME = "HH:mm:ss"
    val LOCALE: Locale = Locale.ROOT

    fun parseToMillis(dateStr: String): Long {
        return try {
            val formatter = SimpleDateFormat(PATTERN_DATE_TIME, LOCALE)
            formatter.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}