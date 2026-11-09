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
}