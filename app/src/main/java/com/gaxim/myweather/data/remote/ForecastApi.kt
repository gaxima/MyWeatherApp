package com.gaxim.myweather.data.remote

import com.gaxim.myweather.data.remote.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS,
        @Query("timezone") timezone: String = "auto",
    ): ForecastResponseDto

    companion object {
        const val FORECAST_DAYS = 7
        const val DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min," +
            "precipitation_sum,snowfall_sum,wind_speed_10m_max,wind_gusts_10m_max"
    }
}
