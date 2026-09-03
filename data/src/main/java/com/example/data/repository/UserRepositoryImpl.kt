package com.example.data.repository

import android.util.Log
import com.example.data.exception.DataException
import com.example.data.exception.NetworkException
import com.example.data.local.UserLocalDataSource
import com.example.data.mapper.UserMapper
import com.example.data.remote.UserRemoteDataSource
import com.example.domain.exception.UserNotFoundException
import com.example.domain.model.UserModel
import com.example.domain.repository.UserRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Убедитесь, что репозиторий Singleton, чтобы кэш работал
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val mapper: UserMapper
) : UserRepository {

    // ✅ Используем ConcurrentHashMap для потокобезопасности
    private val userCache = ConcurrentHashMap<Int, UserModel>()

    override suspend fun getUser(id: Int): UserModel {
        // 1. Проверяем кэш
        userCache[id]?.let { return it }

        return try {
            // 2. Пытаемся получить из сети
            val dto = remoteDataSource.getUser(id)
            mapper.toModel(dto).also { userCache[id] = it }

        } catch (e: UserNotFoundException) {
            // 3. Если сеть вернула 404 (UserNotFoundException), пробрасываем его дальше
            throw e

        } catch (e: NetworkException) {
            // 4. При сетевой ошибке используем локальный fallback
            Log.w("UserRepository", "Network error, trying local cache for user $id", e)
            try {
                val dto = localDataSource.getUserFromAssets("user$id.json")
                mapper.toModel(dto).also { userCache[id] = it }
            } catch (localEx: UserNotFoundException) {
                // Файл не найден в assets (UserLocalDataSource выбрасывает именно его при FileNotFoundException)
                throw UserNotFoundException(
                    userId = id,
                    message = "Пользователь не найден в сети и локальном кэше",
                    cause = localEx
                )
            } catch (localEx: Exception) {
                // Другие ошибки локального кэша (например, DataParsingException при ошибке JSON)
                Log.e("UserRepository", "Local cache parsing failed for user $id", localEx)
                throw DataException(
                    message = "Ошибка чтения локальных данных для пользователя $id",
                    cause = localEx
                )
            }
        } catch (e: Exception) {
            // 5. Любые другие непредвиденные ошибки
            throw DataException(
                message = "Неожиданная ошибка при получении пользователя $id: ${e.message}",
                cause = e
            )
        }
    }

    // ✅ Метод для очистки кэша (вызывать при логауте из AuthRepositoryImpl)
    fun clearCache() {
        userCache.clear()
        Log.d("UserRepository", "User cache cleared")
    }
}