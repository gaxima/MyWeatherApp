package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor

/**
 * Tunable constants for skiing. Units: cm (snowfall), °C (temperature), km/h (wind).
 * Weights sum to 100 and express relative importance.
 */
internal object SkiingThresholds {
    const val SNOWFALL_WEIGHT = 40.0
    const val TEMPERATURE_WEIGHT = 30.0
    const val WIND_WEIGHT = 20.0
    const val GUST_WEIGHT = 10.0

    /** Daily snowfall at or below this means no skiing: the whole score is 0, whatever the wind. */
    const val SNOWFALL_NONE_CM = 0.0

    /** Daily snowfall at or above this earns full snowfall points and counts as "fresh snow". */
    const val SNOWFALL_IDEAL_CM = 5.0

    /** Daily max at or below this keeps snow in good condition: full temperature points. */
    const val TEMP_IDEAL_MAX_C = 0.0

    /** Daily max at or above this melts snow: zero temperature points, reason "too warm". */
    const val TEMP_WARM_LIMIT_C = 5.0

    /** Sustained wind up to this is comfortable; at [WIND_LIMIT_KMH] lifts close. */
    const val WIND_IDEAL_KMH = 20.0
    const val WIND_LIMIT_KMH = 60.0

    const val GUST_IDEAL_KMH = 40.0
    const val GUST_LIMIT_KMH = 80.0
}

object SkiingScorer : ActivityScorer {

    override fun score(forecast: DailyForecast): ActivityScore {
        val t = SkiingThresholds
        val builder = ScoreBuilder()
        builder.factor(
            WeatherFactor.SNOWFALL,
            t.SNOWFALL_WEIGHT,
            ramp(forecast.snowfallSum, t.SNOWFALL_NONE_CM, t.SNOWFALL_IDEAL_CM),
        )
        builder.factor(
            WeatherFactor.TEMPERATURE,
            t.TEMPERATURE_WEIGHT,
            ramp(forecast.temperatureMax, t.TEMP_WARM_LIMIT_C, t.TEMP_IDEAL_MAX_C),
        )
        builder.factor(
            WeatherFactor.WIND_SPEED,
            t.WIND_WEIGHT,
            ramp(forecast.windSpeedMax, t.WIND_LIMIT_KMH, t.WIND_IDEAL_KMH),
        )
        builder.factor(
            WeatherFactor.WIND_GUSTS,
            t.GUST_WEIGHT,
            ramp(forecast.windGustsMax, t.GUST_LIMIT_KMH, t.GUST_IDEAL_KMH),
        )
        return ActivityScore(
            activity = Activity.SKIING,
            score = if (hasNoSnow(forecast)) 0 else builder.score(),
            reason = reasonFor(forecast),
            missingFactors = builder.missingFactors,
        )
    }

    private fun hasNoSnow(forecast: DailyForecast): Boolean =
        forecast.snowfallSum?.let { it <= SkiingThresholds.SNOWFALL_NONE_CM } == true

    private fun reasonFor(forecast: DailyForecast): ReasonKey {
        val t = SkiingThresholds
        val wind = forecast.windSpeedMax
        val gusts = forecast.windGustsMax
        val tempMax = forecast.temperatureMax
        val snow = forecast.snowfallSum
        return when {
            hasNoSnow(forecast) -> ReasonKey.SKI_NO_SNOW
            wind != null && wind >= t.WIND_LIMIT_KMH -> ReasonKey.STRONG_WIND
            gusts != null && gusts >= t.GUST_LIMIT_KMH -> ReasonKey.STRONG_WIND
            tempMax != null && tempMax >= t.TEMP_WARM_LIMIT_C -> ReasonKey.SKI_TOO_WARM
            snow == null -> ReasonKey.INSUFFICIENT_DATA
            snow >= t.SNOWFALL_IDEAL_CM -> ReasonKey.SKI_FRESH_SNOW
            else -> ReasonKey.SKI_LITTLE_FRESH_SNOW
        }
    }
}
