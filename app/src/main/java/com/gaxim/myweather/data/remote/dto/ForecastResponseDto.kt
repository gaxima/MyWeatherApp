package com.gaxim.myweather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val daily: DailyDto? = null,
)

/**
 * Open-Meteo returns one parallel array per field, indexed by day. Values can be null when the
 * provider has no data, and arrays are not guaranteed to be the same length.
 */
@Serializable
data class DailyDto(
    val time: List<String> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?> = emptyList(),
    @SerialName("precipitation_sum") val precipitationSum: List<Double?> = emptyList(),
    @SerialName("snowfall_sum") val snowfallSum: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double?> = emptyList(),
    @SerialName("wind_gusts_10m_max") val windGustsMax: List<Double?> = emptyList(),
)
