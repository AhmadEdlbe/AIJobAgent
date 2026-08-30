package com.example.aijobagent.di

import com.example.aijobagent.core.security.EncryptedPrefs
import com.example.aijobagent.data.remote.api.BackendApiService
import com.example.aijobagent.data.remote.api.OpenAiApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthInterceptor(prefs: EncryptedPrefs): Interceptor = Interceptor { chain ->
        val token = prefs.getString("jwt_access_token", null)
        val req = if (!token.isNullOrBlank()) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else chain.request()
        chain.proceed(req)
    }

    @Provides
    @Singleton
    @Named("backendOkHttp")
    fun provideBackendOkHttp(
        logging: HttpLoggingInterceptor,
        @Named("auth") auth: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    @Named("openAiOkHttp")
    fun provideOpenAiOkHttp(logging: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    @Named("backendRetrofit")
    fun provideBackendRetrofit(@Named("backendOkHttp") client: OkHttpClient, moshi: Moshi): Retrofit {
        // Default to local backend; override via EncryptedPrefs key "backend_url"
        val base = "http://10.0.2.2:8080/api/v1/" // emulator localhost
        return Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("openAiRetrofit")
    fun provideOpenAiRetrofit(@Named("openAiOkHttp") client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Legacy single retrofit binding for backward compat
    @Provides
    @Singleton
    fun provideRetrofit(@Named("openAiRetrofit") retrofit: Retrofit): Retrofit = retrofit

    @Provides
    @Singleton
    fun provideBackendApi(@Named("backendRetrofit") retrofit: Retrofit): BackendApiService =
        retrofit.create(BackendApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenAiApi(@Named("openAiRetrofit") retrofit: Retrofit): OpenAiApiService =
        retrofit.create(OpenAiApiService::class.java)
}
