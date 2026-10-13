package com.example.data.repository

import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.toDomain
import com.example.data.remote.ProductRemoteDataSource
import com.example.data.util.safeRepositoryCall
import com.example.domain.exception.DstPointNotFoundException
import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : ProductRepository {

    override suspend fun getProductList(zone: Int): List<Product> {
        val dto = safeRepositoryCall(
            contextMessage = "Ошибка при получении точки выгрузки бетона",
            onAuthError = { localDataSource.clearSession() },
            notFoundMapper = {
                DstPointNotFoundException(
                    zone = zone,
                    message = it.message ?: "Точка выгрузки бетона не найдена"
                )
            }
        ) {
            remoteDataSource.getProductList(zone)
        }
        return dto.map { it.toDomain() }
    }
}