package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor

/**
 * Tunable constants describing how pleasant a day is outdoors. Shared by outdoor and indoor
 * sightseeing, since indoor is the inverse. Units: °C, mm, km/h. Outdoor weights sum to 100.
 */
internal object OutdoorThresholds {
    const val WEATHER_CODE_WEIGHT = 30.0
    const val PRECIPITATION_WEIGHT = 25.0
    const val TEMPERATURE_WEIGHT = 25.0
    const val WIND_WEIGHT = 10.0
    const val GUST_WEIGHT = 10.0

    /** Daily max at or below [TEMP_ZERO_LOW_C] / at or above [TEMP_ZERO_HIGH_C] earns no points. */
    const val TEMP_ZERO_LOW_C = 0.0
    const val TEMP_ZERO_HIGH_C = 38.0

    /** Comfortable band: full temperature points between these two values. */
    const val TEMP_FULL_LOW_C = 15.0
    const val TEMP_FULL_HIGH_C = 28.0

    const val PRECIP_IDEAL_MM = 0.5

    /** Daily precipitation at or above this ruins a day outdoors: zero points, reason "rain". */
    const val PRECIP_LIMIT_MM = 8.0

    const val WIND_IDEAL_KMH = 25.0
    const val WIND_LIMIT_KMH = 55.0

    const val GUST_IDEAL_KMH = 45.0
    const val GUST_LIMIT_KMH = 80.0
}

/** Per-factor outdoor comfort, 0..1, or null when the input is missing. */
internal data class OutdoorFractions(
    val weatherCode: Double?,
    val precipitation: Double?,
    val temperature: Double?,
    val wind: Double?,
    val gusts: Double?,
)

internal fun outdoorFractions(forecast: DailyForecast): OutdoorFractions {
    val t = OutdoorThresholds
    return OutdoorFractions(
        weatherCode = WeatherCodes.outdoorFriendliness(forecast.weatherCode),
        precipitation = ramp(forecast.precipitationSum, t.PRECIP_LIMIT_MM, t.PRECIP_IDEAL_MM),
        temperature = band(
            forecast.temperatureMax,
            t.TEMP_ZERO_LOW_C,
            t.TEMP_FULL_LOW_C,
            t.TEMP_FULL_HIGH_C,
            t.TEMP_ZERO_HIGH_C,
        ),
        wind = ramp(forecast.windSpeedMax, t.WIND_LIMIT_KMH, t.WIND_IDEAL_KMH),
        gusts = ramp(forecast.windGustsMax, t.GUST_LIMIT_KMH, t.GUST_IDEAL_KMH),
    )
}

/**
 * The main thing that makes the day bad outdoors, or null if nothing does. First matching rule
 * wins, from most to least severe.
 */
internal fun outdoorHazard(forecast: DailyForecast): ReasonKey? {
    val t = OutdoorThresholds
    val precip = forecast.precipitationSum
    val wind = forecast.windSpeedMax
    val gusts = forecast.windGustsMax
    val tempMax = forecast.temperatureMax
    return when {
        WeatherCodes.isThunderstorm(forecast.weatherCode) -> ReasonKey.STORM
        precip != null && precip >= t.PRECIP_LIMIT_MM -> ReasonKey.RAIN
        wind != null && wind >= t.WIND_LIMIT_KMH -> ReasonKey.STRONG_WIND
        gusts != null && gusts >= t.GUST_LIMIT_KMH -> ReasonKey.STRONG_WIND
        tempMax != null && tempMax < t.TEMP_FULL_LOW_C -> ReasonKey.OUTDOOR_TOO_COLD
        tempMax != null && tempMax > t.TEMP_FULL_HIGH_C -> ReasonKey.OUTDOOR_TOO_HOT
        else -> null
    }
}

object OutdoorSightseeingScorer : ActivityScorer {

    override fun score(forecast: DailyForecast): ActivityScore {
        val t = OutdoorThresholds
        val fractions = outdoorFractions(forecast)
        val builder = ScoreBuilder()
        builder.factor(WeatherFactor.WEATHER_CODE, t.WEATHER_CODE_WEIGHT, fractions.weatherCode)
        builder.factor(WeatherFactor.PRECIPITATION, t.PRECIPITATION_WEIGHT, fractions.precipitation)
        builder.factor(WeatherFactor.TEMPERATURE, t.TEMPERATURE_WEIGHT, fractions.temperature)
        builder.factor(WeatherFactor.WIND_SPEED, t.WIND_WEIGHT, fractions.wind)
        builder.factor(WeatherFactor.WIND_GUSTS, t.GUST_WEIGHT, fractions.gusts)
        return ActivityScore(
            activity = Activity.OUTDOOR_SIGHTSEEING,
            score = builder.score(),
            reason = outdoorHazard(forecast) ?: noHazardReason(forecast),
            missingFactors = builder.missingFactors,
        )
    }

    /** With no hazard, "pleasant" is only honest when temperature, the decisive factor, is known. */
    private fun noHazardReason(forecast: DailyForecast): ReasonKey =
        if (forecast.temperatureMax == null) ReasonKey.INSUFFICIENT_DATA else ReasonKey.OUTDOOR_PLEASANT
}
