package com.example.data.repository

import com.example.data.exception.*
import com.example.data.local.AuthLocalDataSource
import com.example.data.mapper.DstPointMapper
import com.example.data.remote.DstPointRemoteDataSource
import com.example.domain.exception.*
import com.example.domain.model.DstPoint
import com.example.domain.repository.DstPointRepository

class DstPointRepositoryImpl (
    private val remoteDataSource: DstPointRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val mapper: DstPointMapper
) : DstPointRepository {

    override suspend fun getDstPoint(zoneId: Int): DstPoint {
        val dto = try {
            remoteDataSource.getDstPoint(zoneId)
        } catch (e: UnauthorizedException) {
            localDataSource.clearSession()
            throw AuthenticationFailedException(
                message = e.message ?: "Ошибка авторизации",
                cause = e
            )
        } catch (e: SessionExpiredException) {
            localDataSource.clearSession()
            throw SessionExpiredDomainException(
                message = e.message ?: "Сессия истекла",
                cause = e
            )
        } catch (e: NetworkException) {
            throw NetworkConnectionException(
                message = e.message ?: "Нет подключения к интернету",
                cause = e
            )
        } catch (e: ServerException) {
            throw ServerUnavailableException(
                message = e.message ?: "Ошибка сервера",
                cause = e
            )
        } catch (e: ApiException) {
            throw ServerApiException(
                errorNumber = e.errorNumber,
                message = e.message ?: "Ошибка API",
                cause = e
            )
        } catch (e: DataLayerException) {
            if (e.errorCode == 404) {
                throw DstPointNotFoundException(
                    zoneId = zoneId,
                    message = e.message ?: "Точка выгрузки бетона не найдена"
                )
            }
            throw AppException(
                message = "Ошибка сети при получении данных точки выгрузки бетона (код: ${e.errorCode})",
                cause = e
            )
        } catch (e: DstPointNotFoundException) {
            // Уже доменное исключение — пробрасываем без повторной обёртки
            throw e
        } catch (e: Exception) {
            throw AppException(
                message = "Неожиданная ошибка при получении точки выгрузки бетона $zoneId: ${e.message ?: "неизвестная ошибка"}",
                cause = e
            )
        }

        return mapper.toModel(dto)
    }
}