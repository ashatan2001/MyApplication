package com.example.data.repository

import android.content.Context
import com.example.data.local.UserLocalDataSource
import com.example.data.mapper.UserMapper
import com.example.data.remote.UserRemoteDataSource
import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import com.example.domain.exception.DataException
import com.example.domain.exception.NetworkException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserRepositoryImpl
    @Inject
    constructor(
        //private val api: UserApi,
        //@ApplicationContext private val context: Context,
        private val remoteDataSource: UserRemoteDataSource,
        private val localDataSource: UserLocalDataSource,
        private val mapper: UserMapper
    ) : UserRepository {
        // Получение из локального JSON
        /*private val json = Json { ignoreUnknownKeys = true }

        override suspend fun getUserFromJson(id: Int): UserModel =
            withContext(Dispatchers.IO) {
                val fileName = "user1.json"
                val jsonString = context.assets
                    .open(fileName)
                    .bufferedReader().use { it.readText() }
                val dto = json.decodeFromString<UserDto>(jsonString)
                mapper.toModel(dto)
            }*/

    override suspend fun getUser(id: Int): UserModel {
        return try {
            // Пытаемся получить из сети
            val dto = remoteDataSource.getUser(id)
            mapper.toModel(dto)
        } catch (e: NetworkException) {
            // При сетевой ошибке используем локальные данные
            try {
                val dto = localDataSource.getUserFromAssets("user$id.json")
                mapper.toModel(dto)
            } catch (localEx: Exception) {
                // Если локальных данных нет - пробрасываем оригинальную ошибку
                throw DataException("Failed to get user $id from network and cache", e)
            }
        } catch (e: Exception) {
            throw DataException("Failed to get user $id", e)
        }
    }
}