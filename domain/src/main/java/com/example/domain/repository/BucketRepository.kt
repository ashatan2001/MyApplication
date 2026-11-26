package com.example.domain.repository

import com.example.domain.model.Bucket

/**
 * Репозиторий для работы с бетоновозными телегами.
 */
interface BucketRepository {
    /**
     * Возвращает список бетоновозных телег.
     *
     * @return [List<Bucket>] список бетоновозных телег
     */
    suspend fun getBucketList(): List<Bucket>

    /**
     * Отправляет команду начала разгрузки бетоновозной телеги.
     *
     * @param id код бетоновозной телеги.
     */
    suspend fun openBucket(id: Int)

    /**
     * Отправляет команду завершения разгрузки бетоновозной телеги.
     *
     * @param id код бетоновозной телеги.
     */
    suspend fun closeBucket(id: Int)
}