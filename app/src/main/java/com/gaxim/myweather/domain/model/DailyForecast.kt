package com.gaxim.myweather.domain.model

import java.time.LocalDate

/**
 * Measurements are nullable because the provider may omit them. Units follow the Open-Meteo
 * defaults: °C, mm, cm (snowfall), km/h, and [weatherCode] as a WMO code.
 */
data class DailyForecast(
    val date: LocalDate,
    val weatherCode: Int?,
    val temperatureMax: Double?,
    val temperatureMin: Double?,
    val precipitationSum: Double?,
    val snowfallSum: Double?,
    val windSpeedMax: Double?,
    val windGustsMax: Double?,
)
