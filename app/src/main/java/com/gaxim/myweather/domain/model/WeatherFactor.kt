package com.gaxim.myweather.domain.model

/**
 * A forecast input that scoring can take into account. Used to tell the user which
 * factors were unavailable (null in the forecast) and therefore not considered.
 */
enum class WeatherFactor {
    WEATHER_CODE,
    TEMPERATURE,
    PRECIPITATION,
    SNOWFALL,
    WIND_SPEED,
    WIND_GUSTS,
}
