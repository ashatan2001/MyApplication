package com.example.data.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.BuildConfig
import com.example.data.network.CustomCookieJar
import com.example.data.network.HeadersInterceptor
import com.example.data.network.TokenInterceptor
import com.example.data.remote.ArchiveApi
import com.example.data.remote.AuthApi
import com.example.data.remote.DstPointsApi
import com.example.data.remote.PersonApi
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
 * - [Base] — для публичных запросов без авторизации.
 * - [Auth] — для запросов, требующих куки и автообновление токена.
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
 *
 * Предоставляет два изолированных HTTP-стека:
 * - **Base** — без кук и токен-интерсептора (регистрация, публичные эндпоинты).
 * - **Auth** — с [CustomCookieJar] и [TokenInterceptor] для авторизованных запросов.
 *
 * Разделение необходимо, чтобы [TokenInterceptor] не перехватывал запросы на обновление токена,
 * что привело бы к бесконечной рекурсии.
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
     * Используется для эндпоинтов, не требующих авторизации.
     */
    @Provides
    @Singleton
    @BaseOkHttp
    fun provideBaseOkHttpClient(
        headersInterceptor: HeadersInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(headersInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * OkHttpClient с аутентификацией.
     *
     * Порядок интерсепторов важен:
     * 1. [CustomCookieJar] — добавляет куки из хранилища.
     * 2. [HeadersInterceptor] — добавляет общие заголовки.
     * 3. [TokenInterceptor] — перехватывает 401/419 и обновляет токен.
     * 4. [HttpLoggingInterceptor] — логирование (только в DEBUG).
     */
    @Provides
    @Singleton
    @AuthOkHttp
    fun provideAuthOkHttpClient(
        cookieJar: CustomCookieJar,
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
            .addInterceptor(headersInterceptor)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /** Retrofit для публичных API без аутентификации. */
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

    /** Retrofit для защищённых API с аутентификацией через куки. */
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

    /** API для получения данных о точках выгрузки бетона. */
    @Provides
    @Singleton
    fun provideDstPointsApi(@AuthRetrofit retrofit: Retrofit): DstPointsApi =
        retrofit.create(DstPointsApi::class.java)

    /** API для получения архивов системы. */
    @Provides
    @Singleton
    fun provideArchiveApi(@AuthRetrofit retrofit: Retrofit): ArchiveApi =
        retrofit.create(ArchiveApi::class.java)
}