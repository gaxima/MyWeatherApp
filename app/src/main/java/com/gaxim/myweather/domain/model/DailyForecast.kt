package com.gaxim.myweather.domain.model

import java.time.LocalDate

/**
 * Forecast for one calendar day at the city's local time.
 *
 * Every measurement is nullable because the provider may omit or null individual values.
 * Units follow the Open-Meteo defaults: temperatures in °C, precipitation in mm,
 * snowfall in cm, wind speeds in km/h, [weatherCode] as a WMO code.
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
