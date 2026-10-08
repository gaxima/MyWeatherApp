package com.gaxim.myweather.data.remote.mapper

import com.gaxim.myweather.data.remote.dto.DailyDto
import com.gaxim.myweather.data.remote.dto.ForecastResponseDto
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.DomainError
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * One [DailyForecast] per entry in `time`. A measurement array that is missing or shorter than
 * `time` yields null for the affected days.
 *
 * @throws DomainError.InvalidResponse if `daily` is absent or a date cannot be parsed.
 */
fun ForecastResponseDto.toDomain(): List<DailyForecast> {
    val daily = daily ?: throw DomainError.InvalidResponse("Forecast response has no daily data")
    return daily.time.indices.map { daily.toForecast(it) }
}

private fun DailyDto.toForecast(index: Int) = DailyForecast(
    date = parseDate(time[index]),
    weatherCode = weatherCode.getOrNull(index),
    temperatureMax = temperatureMax.getOrNull(index),
    temperatureMin = temperatureMin.getOrNull(index),
    precipitationSum = precipitationSum.getOrNull(index),
    snowfallSum = snowfallSum.getOrNull(index),
    windSpeedMax = windSpeedMax.getOrNull(index),
    windGustsMax = windGustsMax.getOrNull(index),
)

private fun parseDate(value: String): LocalDate = try {
    LocalDate.parse(value)
} catch (e: DateTimeParseException) {
    throw DomainError.InvalidResponse("Invalid forecast date: $value", e)
}
