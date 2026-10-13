package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor

/**
 * Tunable constants for surfing. Units: km/h (wind), mm (precipitation), °C (temperature).
 *
 * ASSUMPTION: no marine/wave API is allowed, so wind is a proxy for surf quality. Some wind
 * builds swell (dead calm is flat), but too much blows the surf out. Wind direction is not
 * available, so the ideal band is a rough compromise. Weights sum to 100.
 */
internal object SurfingThresholds {
    const val WIND_WEIGHT = 35.0
    const val GUST_WEIGHT = 15.0
    const val PRECIPITATION_WEIGHT = 20.0
    const val WEATHER_CODE_WEIGHT = 20.0
    const val TEMPERATURE_WEIGHT = 10.0

    /** At or below this wind there is effectively no surf (flat). */
    const val WIND_ZERO_LOW_KMH = 5.0

    const val WIND_FULL_LOW_KMH = 15.0
    const val WIND_FULL_HIGH_KMH = 30.0

    /** At or above this wind the surf is blown out: zero wind points. */
    const val WIND_ZERO_HIGH_KMH = 50.0

    const val GUST_IDEAL_KMH = 40.0
    const val GUST_LIMIT_KMH = 70.0

    const val PRECIP_IDEAL_MM = 1.0
    const val PRECIP_LIMIT_MM = 10.0

    /** Daily max at or below this is too cold to enjoy: zero temperature points. */
    const val TEMP_COLD_C = 5.0

    /** Daily max at or above this is comfortable: full temperature points. */
    const val TEMP_COMFORT_C = 15.0
}

object SurfingScorer : ActivityScorer {

    override fun score(forecast: DailyForecast): ActivityScore {
        val surfingThresholds = SurfingThresholds
        val builder = ScoreBuilder()
        builder.factor(
            WeatherFactor.WIND_SPEED,
            surfingThresholds.WIND_WEIGHT,
            band(
                forecast.windSpeedMax,
                surfingThresholds.WIND_ZERO_LOW_KMH,
                surfingThresholds.WIND_FULL_LOW_KMH,
                surfingThresholds.WIND_FULL_HIGH_KMH,
                surfingThresholds.WIND_ZERO_HIGH_KMH,
            ),
        )
        builder.factor(
            WeatherFactor.WIND_GUSTS,
            surfingThresholds.GUST_WEIGHT,
            ramp(forecast.windGustsMax, surfingThresholds.GUST_LIMIT_KMH, surfingThresholds.GUST_IDEAL_KMH),
        )
        builder.factor(
            WeatherFactor.PRECIPITATION,
            surfingThresholds.PRECIPITATION_WEIGHT,
            ramp(forecast.precipitationSum, surfingThresholds.PRECIP_LIMIT_MM, surfingThresholds.PRECIP_IDEAL_MM),
        )
        builder.factor(
            WeatherFactor.WEATHER_CODE,
            surfingThresholds.WEATHER_CODE_WEIGHT,
            WeatherCodes.outdoorFriendliness(forecast.weatherCode),
        )
        builder.factor(
            WeatherFactor.TEMPERATURE,
            surfingThresholds.TEMPERATURE_WEIGHT,
            ramp(forecast.temperatureMax, surfingThresholds.TEMP_COLD_C, surfingThresholds.TEMP_COMFORT_C),
        )
        return ActivityScore(
            activity = Activity.SURFING,
            score = builder.score(),
            reason = reasonFor(forecast),
            missingFactors = builder.missingFactors,
        )
    }

    /**
     * First matching rule wins: danger (storm, wind, rain) outranks wind quality. The wind reasons
     * follow the wind band used for the score: too calm below it, choppy above it, strong wind
     * only once the surf is blown out.
     */
    private fun reasonFor(forecast: DailyForecast): ReasonKey {
        val surfingThresholds = SurfingThresholds
        val wind = forecast.windSpeedMax
        val gusts = forecast.windGustsMax
        val precip = forecast.precipitationSum
        return when {
            WeatherCodes.isThunderstorm(forecast.weatherCode) -> ReasonKey.STORM
            wind != null && wind >= surfingThresholds.WIND_ZERO_HIGH_KMH -> ReasonKey.STRONG_WIND
            gusts != null && gusts >= surfingThresholds.GUST_LIMIT_KMH -> ReasonKey.STRONG_WIND
            precip != null && precip >= surfingThresholds.PRECIP_LIMIT_MM -> ReasonKey.RAIN
            wind == null -> ReasonKey.INSUFFICIENT_DATA
            wind < surfingThresholds.WIND_FULL_LOW_KMH -> ReasonKey.SURF_TOO_CALM
            wind > surfingThresholds.WIND_FULL_HIGH_KMH -> ReasonKey.SURF_CHOPPY
            else -> ReasonKey.SURF_RIDEABLE_WIND
        }
    }
}
