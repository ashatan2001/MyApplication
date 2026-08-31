package com.example.data.local

import android.content.Context
import com.example.data.dto.UserDto
import com.example.domain.exception.DataParsingException
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

    /**
     * Читает пользователя из assets по имени файла.
     * @throws UserNotFoundException если файл не найден
     * @throws DataParsingException если JSON некорректен
     */
    suspend fun getUserFromAssets(fileName: String): UserDto = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }

            json.decodeFromString<UserDto>(jsonString)
        } catch (e: FileNotFoundException) {
            throw UserNotFoundException("File not found: $fileName", e)
        } catch (e: SerializationException) {
            throw DataParsingException("Failed to parse user JSON from $fileName", e)
        } catch (e: IOException) {
            throw DataParsingException("I/O error while reading $fileName", e)
        }
    }
}