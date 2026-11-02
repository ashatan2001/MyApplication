package com.example.data.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.BuildConfig
import com.example.data.network.CustomCookieJar
import com.example.data.network.DynamicBaseUrlInterceptor
import com.example.data.network.HeadersInterceptor
import com.example.data.network.TokenInterceptor
import com.example.data.remote.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

// region Квалификаторы
/**
 * Квалификаторы разделяют два набора HTTP-клиентов:
 * - [BaseOkHttp] / [BaseRetrofit] — для публичных запросов без авторизации.
 * - [AuthOkHttp] / [AuthRetrofit] — для запросов, требующих куки и автообновление токена.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit
// endregion

/** Локальное хранилище кук аутентификации через DataStore. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_cookies")

/**
 * Модуль сетевых зависимостей.
 * Предоставляет два изолированных HTTP-стека:
 * - **Base** — без кук и токен-интерсептора (регистрация, публичные эндпоинты).
 * - **Auth** — с [CustomCookieJar] и [TokenInterceptor] для авторизованных запросов.
 *
 * В оба клиента добавлен [DynamicBaseUrlInterceptor] **первым** в цепочке,
 * что позволяет динамически менять адрес сервера без пересоздания клиентов.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Базовый OkHttpClient без аутентификации.
     *
     * Порядок интерсепторов:
     * 1. [DynamicBaseUrlInterceptor] — подменяет хост/порт на актуальный.
     * 2. [HeadersInterceptor] — добавляет общие заголовки (Accept с версией API).
     */
    @Provides
    @Singleton
    @BaseOkHttp
    fun provideBaseOkHttpClient(
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        headersInterceptor: HeadersInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(dynamicBaseUrlInterceptor)
        .addInterceptor(headersInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * OkHttpClient с аутентификацией.
     *
     * Порядок интерсепторов критически важен:
     * 1. [DynamicBaseUrlInterceptor] — подменяет хост/порт на актуальный.
     * 2. [HeadersInterceptor] — добавляет общие заголовки.
     * 3. [TokenInterceptor] — перехватывает 401/419 и обновляет токен.
     * 4. [HttpLoggingInterceptor] — логирование (только в DEBUG).
     */
    @Provides
    @Singleton
    @AuthOkHttp
    fun provideAuthOkHttpClient(
        cookieJar: CustomCookieJar,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        headersInterceptor: HeadersInterceptor,
        tokenInterceptor: TokenInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        Log.d("LOGIN_DEBUG", "[NetworkModule] provideAuthOkHttpClient")
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(headersInterceptor)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Retrofit для публичных API без аутентификации.
     *
     * [BuildConfig.BASE_URL] используется только для инициализации Retrofit.
     * Реальный хост подменяется [DynamicBaseUrlInterceptor] перед отправкой запроса.
     */
    @Provides
    @Singleton
    @BaseRetrofit
    fun provideBaseRetrofit(
        @BaseOkHttp okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /**
     * Retrofit для защищённых API с аутентификацией через куки.
     *
     * [BuildConfig.BASE_URL] используется только для инициализации Retrofit.
     * Реальный хост подменяется [DynamicBaseUrlInterceptor] перед отправкой запроса.
     */
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        @AuthOkHttp client: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /** API для операций аутентификации (логин, обновление токена). */
    @Provides
    @Singleton
    fun provideAuthApi(@AuthRetrofit retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    /** API для получения данных пользователя. */
    @Provides
    @Singleton
    fun provideUserApi(@AuthRetrofit retrofit: Retrofit): PersonApi =
        retrofit.create(PersonApi::class.java)

    /** API для получения точек выгрузки. */
    @Provides
    @Singleton
    fun provideDstPointApi(@AuthRetrofit retrofit: Retrofit): DstPointApi =
        retrofit.create(DstPointApi::class.java)

    /** API для получения продуктов. */
    @Provides
    @Singleton
    fun provideProductApi(@AuthRetrofit retrofit: Retrofit): ProductApi =
        retrofit.create(ProductApi::class.java)

    /** API для работы с заявками. */
    @Provides
    @Singleton
    fun provideTaskApi(@AuthRetrofit retrofit: Retrofit): TaskApi =
        retrofit.create(TaskApi::class.java)

    /** API для получения заявок на отгрузку, находящихся в очереди. */
    @Provides
    @Singleton
    fun provideQueueApi(@AuthRetrofit retrofit: Retrofit): QueueApi =
        retrofit.create(QueueApi::class.java)

    /** API для получения архивов системы. */
    @Provides
    @Singleton
    fun provideArchiveApi(@AuthRetrofit retrofit: Retrofit): ArchiveApi =
        retrofit.create(ArchiveApi::class.java)
}