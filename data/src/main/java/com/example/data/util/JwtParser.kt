package com.example.data.util

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject

/**
 * Парсер JWT-токенов для извлечения данных пользователя.
 *
 * Извлекает полезную нагрузку (payload) из токена без проверки подписи,
 * так как проверка должна выполняться на сервере.
 */
class JwtParser @Inject constructor(
    private val json: Json
) {

    /**
     * Извлекает ID пользователя из payload JWT-токена.
     *
     * @param jwt Токен в формате `header.payload.signature`.
     * @return ID пользователя или null, если токен невалиден или поле отсутствует.
     */
    fun getUserIdFromToken(jwt: String): Int? {
        return try {
            val parts = jwt.split(".")
            if (parts.size != 3) {
                Timber.w("Invalid JWT format: expected 3 parts, got ${parts.size}")
                return null
            }

            val payloadBase64 = parts[1]
            val payloadBytes = Base64.decode(
                payloadBase64,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            val payloadString = String(payloadBytes, Charsets.UTF_8)
            val jsonElement = json.parseToJsonElement(payloadString)

            // Поле "userID" (а не "userId") — соответствует контракту сервера
            jsonElement.jsonObject["userID"]?.jsonPrimitive?.content?.toIntOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse JWT token")
            null
        }
    }
}