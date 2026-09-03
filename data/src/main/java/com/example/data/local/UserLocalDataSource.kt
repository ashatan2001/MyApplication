package com.example.data.local

import android.content.Context
import com.example.data.dto.UserDto
import com.example.data.exception.DataException
import com.example.data.exception.DataParsingException
import com.example.domain.exception.UserNotFoundException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    suspend fun getUserFromAssets(fileName: String): UserDto = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            json.decodeFromString<UserDto>(jsonString)
        } catch (e: FileNotFoundException) {
            // ✅ Используем UserNotFoundException, который уже есть в domain.exception
            throw UserNotFoundException(
                userId = null,
                message = "Файл не найден: $fileName",
                cause = e
            )
        } catch (e: SerializationException) {
            // ✅ Используем DataParsingException из domain.exception
            throw DataParsingException(
                message = "Ошибка парсинга JSON из $fileName",
                cause = e
            )
        } catch (e: IOException) {
            // ✅ Используем DataException для общих ошибок ввода-вывода
            throw DataException(
                message = "Ошибка ввода-вывода при чтении $fileName",
                cause = e
            )
        }
    }
}