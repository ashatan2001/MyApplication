package com.example.data.remote

import com.example.data.dto.ProductDto
import com.example.data.util.safeApiCall
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API для продуктов, разрешенных для отгрузки.
 *
 * Все методы возвращают [Response], чтобы [safeApiCall]
 * мог получить доступ к коду ответа и телу ошибки для детального маппинга исключений.
 */
interface ProductApi {

    /**
     * Получение списка продуктов.
     *
     * @param zoneid Код точки выгрузки бетона.
     * @return [Response<List<ProductDto>>] с данными продукта, разрешенного для отгрузки в заданную точку выгрузки.
     *
     * Коды ответов сервера:
     * - 200 — успешное выполнение команды (в теле запроса JSON)
     * - 204 — для заданной точки выгрузки не разрешена отгрузка ни одного продукта (тело запроса является пустым)
     * - 400 — некорректно сформирован запрос
     * - 401 — необходимо выполнить вход в систему
     * - 419 — истек срок действия Access Token. Необходимо восстановить его с помощью Refresh Token
     * - 5xx — внутренняя ошибка получения информации о продуктах, разрешенных для заданной зоны
     */
    @GET("api/apb/products/{zoneid}")
    suspend fun getProductList(@Path("zoneid") zone: Int): Response<List<ProductDto>>
}