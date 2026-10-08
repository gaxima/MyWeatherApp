package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor

/**
 * Tunable constants for indoor sightseeing, which gets better as the outdoors gets worse.
 * Weights sum to 100 and lean on rain and sky, the main reasons to go inside.
 */
internal object IndoorThresholds {
    const val WEATHER_CODE_WEIGHT = 35.0
    const val PRECIPITATION_WEIGHT = 40.0
    const val TEMPERATURE_WEIGHT = 15.0
    const val WIND_WEIGHT = 5.0
    const val GUST_WEIGHT = 5.0

    /**
     * Each factor is worth at least this fraction of its weight even in perfect weather, because
     * museums and galleries are always an option. A perfect outdoor day scores exactly this.
     */
    const val BASELINE_FRACTION = 0.3
}

object IndoorSightseeingScorer : ActivityScorer {

    override fun score(forecast: DailyForecast): ActivityScore {
        val t = IndoorThresholds
        val outdoor = outdoorFractions(forecast)
        val builder = ScoreBuilder()
        builder.factor(WeatherFactor.WEATHER_CODE, t.WEATHER_CODE_WEIGHT, indoorFraction(outdoor.weatherCode))
        builder.factor(WeatherFactor.PRECIPITATION, t.PRECIPITATION_WEIGHT, indoorFraction(outdoor.precipitation))
        builder.factor(WeatherFactor.TEMPERATURE, t.TEMPERATURE_WEIGHT, indoorFraction(outdoor.temperature))
        builder.factor(WeatherFactor.WIND_SPEED, t.WIND_WEIGHT, indoorFraction(outdoor.wind))
        builder.factor(WeatherFactor.WIND_GUSTS, t.GUST_WEIGHT, indoorFraction(outdoor.gusts))
        return ActivityScore(
            activity = Activity.INDOOR_SIGHTSEEING,
            score = builder.score(),
            reason = outdoorHazard(forecast) ?: ReasonKey.INDOOR_ANY_WEATHER,
            missingFactors = builder.missingFactors,
        )
    }

    /** Worse outdoors means better indoors, never dropping below the baseline. Null stays null. */
    private fun indoorFraction(outdoorFraction: Double?): Double? = outdoorFraction?.let {
        IndoorThresholds.BASELINE_FRACTION + (1 - IndoorThresholds.BASELINE_FRACTION) * (1 - it)
    }
}
