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

    /** Ideal wind band: full wind points between these two values. */
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
        val t = SurfingThresholds
        val builder = ScoreBuilder()
        builder.factor(
            WeatherFactor.WIND_SPEED,
            t.WIND_WEIGHT,
            band(
                forecast.windSpeedMax,
                t.WIND_ZERO_LOW_KMH,
                t.WIND_FULL_LOW_KMH,
                t.WIND_FULL_HIGH_KMH,
                t.WIND_ZERO_HIGH_KMH,
            ),
        )
        builder.factor(
            WeatherFactor.WIND_GUSTS,
            t.GUST_WEIGHT,
            ramp(forecast.windGustsMax, t.GUST_LIMIT_KMH, t.GUST_IDEAL_KMH),
        )
        builder.factor(
            WeatherFactor.PRECIPITATION,
            t.PRECIPITATION_WEIGHT,
            ramp(forecast.precipitationSum, t.PRECIP_LIMIT_MM, t.PRECIP_IDEAL_MM),
        )
        builder.factor(
            WeatherFactor.WEATHER_CODE,
            t.WEATHER_CODE_WEIGHT,
            WeatherCodes.outdoorFriendliness(forecast.weatherCode),
        )
        builder.factor(
            WeatherFactor.TEMPERATURE,
            t.TEMPERATURE_WEIGHT,
            ramp(forecast.temperatureMax, t.TEMP_COLD_C, t.TEMP_COMFORT_C),
        )
        return ActivityScore(
            activity = Activity.SURFING,
            score = builder.score(),
            reason = reasonFor(forecast),
            missingFactors = builder.missingFactors,
        )
    }

    /** First matching rule wins: danger (storm, wind, rain) outranks wind quality. */
    private fun reasonFor(forecast: DailyForecast): ReasonKey {
        val t = SurfingThresholds
        val wind = forecast.windSpeedMax
        val gusts = forecast.windGustsMax
        val precip = forecast.precipitationSum
        return when {
            WeatherCodes.isThunderstorm(forecast.weatherCode) -> ReasonKey.STORM
            wind != null && wind > t.WIND_FULL_HIGH_KMH -> ReasonKey.STRONG_WIND
            gusts != null && gusts >= t.GUST_LIMIT_KMH -> ReasonKey.STRONG_WIND
            precip != null && precip >= t.PRECIP_LIMIT_MM -> ReasonKey.RAIN
            wind == null -> ReasonKey.INSUFFICIENT_DATA
            wind < t.WIND_FULL_LOW_KMH -> ReasonKey.SURF_TOO_CALM
            else -> ReasonKey.SURF_RIDEABLE_WIND
        }
    }
}
