package com.example.data.network

import com.example.data.BuildConfig
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Перехватчик для динамической подмены базового URL (схемы, хоста и порта) в запросах.
 *
 * Позволяет менять адрес сервера "на лету" без пересоздания экземпляров
 * [okhttp3.OkHttpClient] и [retrofit2.Retrofit], сохраняя пул соединений.
 *
 * **Важно:** Должен добавляться **первым** в цепочку интерсепторов [OkHttpClient],
 * чтобы все последующие интерсепторы (включая [TokenInterceptor]) работали
 * уже с актуальным (новым) URL.
 *
 * Архитектура:
 * - При инициализации подписывается на [SettingsRepository.serverUrl].
 * - Кэширует актуальный URL в памяти ([cachedBaseUrl]).
 * - При каждом запросе подменяет схему, хост и порт на кэшированные значения.
 * - Путь (path) и параметры запроса (query) сохраняются без изменений.
 *
 * @param settingsRepository Репозиторий для получения актуального URL.
 */
@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    /** Scope для фоновой подписки на изменения URL. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Кэшированный актуальный URL.
     * Инициализируется значением из [BuildConfig] на случай ранних запросов
     * до завершения первой загрузки из DataStore.
     *
     * Аннотация [@Volatile] гарантирует видимость изменений между потоками:
     * collect работает в [Dispatchers.IO], а [intercept] вызывается в потоках OkHttp.
     */
    @Volatile
    private var cachedBaseUrl: String = BuildConfig.BASE_URL

    init {
        // Асинхронно обновляем кэш при любом изменении в DataStore
        scope.launch {
            settingsRepository.serverUrl.collect { url ->
                Timber.d("DynamicBaseUrlInterceptor: кэш URL обновлён -> $url")
                cachedBaseUrl = url
            }
        }
    }

    /**
     * Перехватывает исходящий запрос и подменяет хост/порт на актуальные.
     *
     * @param chain Цепочка перехватчиков OkHttp.
     * @return [Response] от сервера.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Безопасный парсинг. toHttpUrlOrNull() возвращает null при некорректном формате
        val targetHttpUrl = cachedBaseUrl.toHttpUrlOrNull()

        // Если парсинг не удался, пропускаем запрос без изменений
        if (targetHttpUrl == null) {
            Timber.w("DynamicBaseUrlInterceptor: не удалось распарсить '$cachedBaseUrl', запрос без изменений")
            return chain.proceed(originalRequest)
        }

        // Формируем новый URL, заменяя только схему, хост и порт.
        // Путь и query-параметры сохраняются оригинальными.
        val newUrl = originalUrl.newBuilder()
            .scheme(targetHttpUrl.scheme)
            .host(targetHttpUrl.host)
            .port(targetHttpUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}