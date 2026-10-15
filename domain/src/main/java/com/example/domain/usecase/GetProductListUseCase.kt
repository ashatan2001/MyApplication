package com.example.domain.usecase

import com.example.domain.exception.AppException
import com.example.domain.exception.DstPointNotFoundException
import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import com.example.domain.util.CustomResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Use-case для получения списка продуктов, разрешенных для отгрузки в заданную точку выгрузки.
 *
 * Инкапсулирует логику запроса списка продуктов из [com.example.domain.repository.ProductRepository]
 * и оборачивает результат в [CustomResult] для безопасной обработки ошибок на уровне Presentation.
 *
 * @param repository Репозиторий для работы со списком точек выгрузки бетона.
 */
class GetProductListUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(zone: Int): CustomResult<List<Product>> {
        return try {
            val dstPointList = repository.getProductList(zone)
            CustomResult.Success(dstPointList)
        } catch (e: CancellationException) {
            throw e
        } catch (e: DstPointNotFoundException) {
            CustomResult.Success(emptyList())
        } catch (e: AppException) {
            CustomResult.Error(e)
        } catch (e: Exception) {
            CustomResult.Error(
                AppException(
                    message = "Ошибка при получении списка продуктов",
                    cause = e
                )
            )
        }
    }
}