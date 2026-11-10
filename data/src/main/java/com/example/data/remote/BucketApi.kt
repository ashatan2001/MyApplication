package com.example.data.remote

import com.example.data.dto.BucketDto
import com.example.data.util.safeApiCall
import retrofit2.Response
import retrofit2.http.POST

/**
 * Retrofit API для операций c заявками на производство ЖБИ.
 *
 * Все методы возвращают [Response], чтобы [safeApiCall]
 * мог получить доступ к коду ответа и телу ошибки для детального маппинга исключений.
 */
interface BucketApi {
    /**
     * Мониторинг состояния бетоновозных телег.
     *
     * @return [Response<List<BucketDto>>] с данными бетоновозных телег.
     *
     * Коды ответов сервера:
     * - 200 — успешное выполнение команды (в теле запроса JSON)
     * - 400 — некорректно сформирован запрос
     * - 401 — необходимо выполнить вход в систему
     * - 403 — пользователь не имеет необходимых прав о состоянии бетоновозных телег
     * - 404 — бетоновозные телеги не найдены в системе.
     * - 419 — истек срок действия Access Token. Необходимо восстановить его с помощью Refresh Token
     * - 5xx — внутренняя ошибка сервера
     */
    @POST("api/apb/buckets")
    suspend fun getBuckets(): Response<List<BucketDto>>

}