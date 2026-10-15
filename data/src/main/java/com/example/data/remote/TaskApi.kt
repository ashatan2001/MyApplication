package com.example.data.remote

import com.example.data.dto.TaskRequestDto
import com.example.data.dto.TaskResponseDto
import com.example.data.util.safeApiCall
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API для операций c заявками на производство ЖБИ.
 *
 * Все методы возвращают [Response], чтобы [safeApiCall]
 * мог получить доступ к коду ответа и телу ошибки для детального маппинга исключений.
 */
interface TaskApi {
    /**
     * Мониторинг текущего состояния очереди на отгрузку.
     *
     * @param body Данные заявки [TaskRequestDto]
     * @return [TaskResponseDto] с данными заявки.
     *
     * Коды ответов сервера:
     * - 200 — успешное выполнение команды (в теле запроса JSON)
     * - 400 — некорректно сформирован запрос
     * - 401 — необходимо выполнить вход в систему
     * - 403 — пользователь не имеет необходимых прав на создание заявок на отгрузку
     * - 404 — один из параметров заявки не может быть идентифицирован базой данных
     * - 419 — истек срок действия Access Token. Необходимо восстановить его с помощью Refresh Token
     * - 5xx — внутренняя ошибка сервера
     */
    @POST("api/apb/task")
    suspend fun createTask(@Body body: TaskRequestDto): Response<TaskResponseDto>

}