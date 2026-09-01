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
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideBaseUrl(): String = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideCookieJar(@ApplicationContext context: Context): CookieJar {
        return CustomCookieJar(context)
    }

    @Provides
    @Singleton
    fun provideOkHttp(cookieJar: CookieJar): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor {
            message -> Log.d("Retrofit", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    // Добавьте токен авторизации если нужно
                    // .header("Authorization", "Bearer ${getToken()}")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthEventBus(): AuthEventBus = AuthEventBusImpl()

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        cookieJar: CustomCookieJar,
        authEventBus: AuthEventBus,
        authApi: AuthApi
    ): Interceptor = AuthInterceptor(cookieJar, authEventBus, authApi)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String,
                        client: OkHttpClient,
                        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)
}
