package com.gaxim.myweather.di

import com.gaxim.myweather.data.remote.ApiJson
import com.gaxim.myweather.data.remote.ForecastApi
import com.gaxim.myweather.data.remote.GeocodingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class GeocodingRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class ForecastRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
    private const val TIMEOUT_SECONDS = 15L

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(client: OkHttpClient): Retrofit =
        retrofit(client, GEOCODING_BASE_URL)

    @Provides
    @Singleton
    @ForecastRetrofit
    fun provideForecastRetrofit(client: OkHttpClient): Retrofit =
        retrofit(client, FORECAST_BASE_URL)

    @Provides
    @Singleton
    fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): GeocodingApi =
        retrofit.create(GeocodingApi::class.java)

    @Provides
    @Singleton
    fun provideForecastApi(@ForecastRetrofit retrofit: Retrofit): ForecastApi =
        retrofit.create(ForecastApi::class.java)

    private fun retrofit(client: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(ApiJson.asConverterFactory("application/json".toMediaType()))
        .build()
}
