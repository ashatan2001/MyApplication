/**
 * Утилитарный объект для валидации URL-адресов.
 * Предоставляет методы проверки формата и автодополнения протокола.
 */
object UrlValidator {

    /**
     * Проверяет корректность формата URL.
     *
     * @param url Строка для проверки.
     * @return Результат валидации: [Valid] или [Invalid] с описанием ошибки.
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    /**
     * Валидирует URL и возвращает результат проверки.
     *
     * @param url Строка URL для проверки.
     * @return [ValidationResult] с результатом проверки.
     */
    fun validate(url: String): ValidationResult {
        if (url.isBlank()) {
            return ValidationResult.Invalid("URL не может быть пустым")
        }

        // Автодополнение протокола, если отсутствует
        val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        return try {
            val uri = java.net.URI(normalizedUrl)
            if (uri.host.isNullOrBlank()) {
                ValidationResult.Invalid("Некорректный хост в URL")
            } else if (uri.scheme != "http" && uri.scheme != "https") {
                ValidationResult.Invalid("Поддерживаются только http и https")
            } else {
                ValidationResult.Valid
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Некорректный формат URL")
        }
    }

    /**
     * Нормализует URL, добавляя протокол при необходимости.
     *
     * @param url Исходный URL.
     * @return Нормализованный URL с протоколом.
     */
    fun normalize(url: String): String {
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
    }
}