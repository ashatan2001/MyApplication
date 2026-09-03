package com.example.data.di

import android.content.Context
import android.util.Log
import com.example.data.BuildConfig
import com.example.data.network.AuthEventBus
import com.example.data.network.AuthEventBusImpl
import com.example.data.network.AuthInterceptor
import com.example.data.network.CustomCookieJar
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

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class BaseOkHttp
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AuthOkHttp
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class BaseRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AuthRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides @Singleton
    fun provideCookieJar(@ApplicationContext context: Context): CustomCookieJar =
        CustomCookieJar(context)

    @Provides @Singleton
    fun provideAuthEventBus(): AuthEventBus = AuthEventBusImpl()

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { Log.d("Retrofit", it) }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    private fun baseBuilder(cookieJar: CustomCookieJar, logging: HttpLoggingInterceptor) =
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .build()
                )
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

    @Provides @Singleton @BaseOkHttp
    fun provideOkHttpClient(cookieJar: CustomCookieJar, logging: HttpLoggingInterceptor): OkHttpClient =
        baseBuilder(cookieJar, logging).build()

    @Provides @Singleton
    fun provideAuthInterceptor(
        cookieJar: CustomCookieJar,
        authEventBus: AuthEventBus,
        authApi: AuthApi
    ): AuthInterceptor = AuthInterceptor(cookieJar, authEventBus, authApi)

    @Provides @Singleton @AuthOkHttp
    fun provideOkHttpClientWithAuth(
        cookieJar: CustomCookieJar,
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient = baseBuilder(cookieJar, logging)
        .addInterceptor(authInterceptor)
        .build()

    private fun buildRetrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton @BaseRetrofit
    fun provideRetrofit(baseUrl: String, @BaseOkHttp client: OkHttpClient, json: Json): Retrofit =
        buildRetrofit(baseUrl, client, json)

    @Provides @Singleton @AuthRetrofit
    fun provideRetrofitWithAuth(baseUrl: String, @AuthOkHttp client: OkHttpClient, json: Json): Retrofit =
        buildRetrofit(baseUrl, client, json)

    @Provides @Singleton
    fun provideAuthApi(@BaseRetrofit retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideUserApi(@AuthRetrofit retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)
}