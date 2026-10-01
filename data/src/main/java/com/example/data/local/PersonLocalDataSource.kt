package com.example.data.local

import android.content.Context
import com.example.data.dto.PersonDto
import com.example.data.exception.DataException
import com.example.data.exception.DataParsingException
import com.example.domain.exception.PersonNotFoundException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

/**
 * Локальный источник данных для чтения информации о пользователе из assets.
 *
 * Используется для оффлайн-режима, моков или начального состояния приложения,
 * когда сетевой запрос еще не выполнен.
 */
class PersonLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    /**
     * Читает и десериализует данные пользователя из JSON-файла в папке assets.
     *
     * @param fileName Имя файла в assets.
     * @return Десериализованный [PersonDto].
     * @throws PersonNotFoundException если файл не найден в assets.
     * @throws DataParsingException если JSON невалиден или не соответствует структуре [PersonDto].
     * @throws DataException при общих ошибках ввода-вывода.
     */
    suspend fun getPersonFromAssets(fileName: String): PersonDto = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            json.decodeFromString<PersonDto>(jsonString)
        } catch (e: FileNotFoundException) {
            throw PersonNotFoundException(
                personId = null,
                message = "Файл не найден: $fileName",
                cause = e
            )
        } catch (e: SerializationException) {
            throw DataParsingException(
                message = "Ошибка парсинга JSON из $fileName",
                cause = e
            )
        } catch (e: IOException) {
            throw DataException(
                message = "Ошибка ввода-вывода при чтении $fileName",
                cause = e
            )
        }
    }
}