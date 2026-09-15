package com.example.data.util

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class JwtParser @Inject constructor(
    private val json: Json
) {
    fun getUserIdFromToken (jwt: String): Int? {
        return try {
            val parts = jwt.split(".")
            if (parts.size != 3) return null

            val payloadBase64 = parts[1]
            val payloadBytes = Base64.decode(payloadBase64, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val payloadString = String(payloadBytes, Charsets.UTF_8)

            val jsonElement = json.parseToJsonElement(payloadString)
            jsonElement.jsonObject["userID"]?.jsonPrimitive?.content?.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }
}