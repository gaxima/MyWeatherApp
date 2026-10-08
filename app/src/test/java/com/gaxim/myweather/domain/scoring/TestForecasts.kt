package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.DailyForecast
import java.time.LocalDate

/**
 * Builds a forecast for tests. Defaults describe a calm, dry, mild, clear day; override only
 * what a test cares about. Pass an explicit `null` to simulate a missing measurement.
 */
internal fun forecast(
    date: LocalDate = LocalDate.of(2026, 1, 15),
    weatherCode: Int? = 0,
    temperatureMax: Double? = 15.0,
    temperatureMin: Double? = 8.0,
    precipitationSum: Double? = 0.0,
    snowfallSum: Double? = 0.0,
    windSpeedMax: Double? = 10.0,
    windGustsMax: Double? = 20.0,
) = DailyForecast(
    date = date,
    weatherCode = weatherCode,
    temperatureMax = temperatureMax,
    temperatureMin = temperatureMin,
    precipitationSum = precipitationSum,
    snowfallSum = snowfallSum,
    windSpeedMax = windSpeedMax,
    windGustsMax = windGustsMax,
)
