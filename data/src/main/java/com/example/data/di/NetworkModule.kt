package com.example.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.BuildConfig
import com.example.data.network.AuthEventBus
import com.example.data.network.AuthEventBusImpl
import com.example.data.network.CustomCookieJar
import com.example.data.network.HeadersInterceptor
import com.example.data.network.TokenAuthenticator
import com.example.data.remote.AuthApi
import com.example.data.remote.UserApi
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

// Квалификаторы для разделения клиентов
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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_cookies")

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

    @Provides
    @Singleton
    @AuthOkHttp
    fun provideAuthOkHttpClient(
        cookieJar: CustomCookieJar,
        headersInterceptor: HeadersInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .authenticator(tokenAuthenticator)
            .addInterceptor(headersInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

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

    @Provides
    @Singleton
    fun provideAuthApi(@AuthRetrofit retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(@AuthRetrofit retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)
}