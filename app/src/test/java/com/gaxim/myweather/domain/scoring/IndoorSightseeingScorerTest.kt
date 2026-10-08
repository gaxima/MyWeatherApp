package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class IndoorSightseeingScorerTest {

    private val baselineScore = (IndoorThresholds.BASELINE_FRACTION * 100).roundToInt()

    private val perfectOutdoorDay = forecast(
        weatherCode = 1,
        temperatureMax = 22.0,
        precipitationSum = 0.0,
        windSpeedMax = 10.0,
        windGustsMax = 20.0,
    )

    @Test
    fun `given perfect outdoor day, when scoring indoor sightseeing, then score sits at the baseline`() {
        val result = IndoorSightseeingScorer.score(perfectOutdoorDay)

        assertEquals(Activity.INDOOR_SIGHTSEEING, result.activity)
        assertEquals(baselineScore, result.score)
        assertEquals(ReasonKey.INDOOR_ANY_WEATHER, result.reason)
        assertTrue(result.missingFactors.isEmpty())
    }

    @Test
    fun `given rainy day, when scoring indoor sightseeing, then score is high and reason is rain`() {
        val result = IndoorSightseeingScorer.score(
            perfectOutdoorDay.copy(weatherCode = 65, precipitationSum = 15.0),
        )

        assertTrue(result.score > 70)
        assertEquals(ReasonKey.RAIN, result.reason)
    }

    @Test
    fun `given thunderstorm and gale, when scoring indoor sightseeing, then score is perfect`() {
        val result = IndoorSightseeingScorer.score(
            forecast(
                weatherCode = 95,
                temperatureMax = -5.0,
                precipitationSum = 30.0,
                windSpeedMax = 90.0,
                windGustsMax = 120.0,
            ),
        )

        assertEquals(100, result.score)
        assertEquals(ReasonKey.STORM, result.reason)
    }

    @Test
    fun `given freezing day, when scoring indoor sightseeing, then reason is outdoor too cold`() {
        val result = IndoorSightseeingScorer.score(perfectOutdoorDay.copy(temperatureMax = -5.0))

        assertEquals(ReasonKey.OUTDOOR_TOO_COLD, result.reason)
    }

    @Test
    fun `given worsening weather, when scoring indoor sightseeing, then score only increases`() {
        val fine = IndoorSightseeingScorer.score(perfectOutdoorDay).score
        val drizzly = IndoorSightseeingScorer.score(
            perfectOutdoorDay.copy(weatherCode = 53, precipitationSum = 3.0),
        ).score
        val stormy = IndoorSightseeingScorer.score(
            perfectOutdoorDay.copy(weatherCode = 95, precipitationSum = 20.0),
        ).score

        assertTrue(fine < drizzly && drizzly < stormy)
    }

    @Test
    fun `given every measurement missing, when scoring indoor sightseeing, then score is neutral`() {
        val result = IndoorSightseeingScorer.score(
            forecast(
                weatherCode = null,
                temperatureMax = null,
                precipitationSum = null,
                windSpeedMax = null,
                windGustsMax = null,
            ),
        )

        assertEquals(ScoreBuilder.NEUTRAL_SCORE, result.score)
        assertEquals(5, result.missingFactors.size)
        assertTrue(WeatherFactor.WEATHER_CODE in result.missingFactors)
    }
}
