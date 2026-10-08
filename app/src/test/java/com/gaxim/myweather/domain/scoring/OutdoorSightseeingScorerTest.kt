package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutdoorSightseeingScorerTest {

    private val t = OutdoorThresholds

    private val idealDay = forecast(
        weatherCode = 1,
        temperatureMax = 22.0,
        precipitationSum = 0.0,
        windSpeedMax = 10.0,
        windGustsMax = 20.0,
    )

    @Test
    fun `given mild dry calm day, when scoring outdoor sightseeing, then score is perfect`() {
        val result = OutdoorSightseeingScorer.score(idealDay)

        assertEquals(Activity.OUTDOOR_SIGHTSEEING, result.activity)
        assertEquals(100, result.score)
        assertEquals(ReasonKey.OUTDOOR_PLEASANT, result.reason)
        assertTrue(result.missingFactors.isEmpty())
    }

    @Test
    fun `given thunderstorm, downpour, freezing and gale, when scoring, then score is zero`() {
        val bad = forecast(
            weatherCode = 95,
            temperatureMax = -5.0,
            precipitationSum = 30.0,
            windSpeedMax = 90.0,
            windGustsMax = 120.0,
        )

        val result = OutdoorSightseeingScorer.score(bad)

        assertEquals(0, result.score)
        assertEquals(ReasonKey.STORM, result.reason)
    }

    @Test
    fun `given temperature exactly at lower comfort edge, when scoring, then it counts fully`() {
        val result = OutdoorSightseeingScorer.score(
            idealDay.copy(temperatureMax = t.TEMP_FULL_LOW_C),
        )

        assertEquals(100, result.score)
        assertEquals(ReasonKey.OUTDOOR_PLEASANT, result.reason)
    }

    @Test
    fun `given temperature below comfort band, when scoring, then score drops and reason is too cold`() {
        val result = OutdoorSightseeingScorer.score(
            idealDay.copy(temperatureMax = t.TEMP_FULL_LOW_C - 1.0),
        )

        assertTrue(result.score < 100)
        assertEquals(ReasonKey.OUTDOOR_TOO_COLD, result.reason)
    }

    @Test
    fun `given temperature exactly at upper comfort edge, when scoring, then it counts fully`() {
        val result = OutdoorSightseeingScorer.score(
            idealDay.copy(temperatureMax = t.TEMP_FULL_HIGH_C),
        )

        assertEquals(100, result.score)
        assertEquals(ReasonKey.OUTDOOR_PLEASANT, result.reason)
    }

    @Test
    fun `given temperature above comfort band, when scoring, then score drops and reason is too hot`() {
        val result = OutdoorSightseeingScorer.score(
            idealDay.copy(temperatureMax = t.TEMP_FULL_HIGH_C + 1.0),
        )

        assertTrue(result.score < 100)
        assertEquals(ReasonKey.OUTDOOR_TOO_HOT, result.reason)
    }

    @Test
    fun `given precipitation exactly at limit, when scoring, then reason is rain`() {
        val result = OutdoorSightseeingScorer.score(
            idealDay.copy(precipitationSum = t.PRECIP_LIMIT_MM),
        )

        assertEquals(ReasonKey.RAIN, result.reason)
    }

    @Test
    fun `given precipitation just below limit, when scoring, then reason is not rain`() {
        val result = OutdoorSightseeingScorer.score(
            idealDay.copy(precipitationSum = t.PRECIP_LIMIT_MM - 0.1),
        )

        assertEquals(ReasonKey.OUTDOOR_PLEASANT, result.reason)
    }

    @Test
    fun `given wind exactly at limit, when scoring, then reason is strong wind`() {
        val result = OutdoorSightseeingScorer.score(idealDay.copy(windSpeedMax = t.WIND_LIMIT_KMH))

        assertEquals(ReasonKey.STRONG_WIND, result.reason)
    }

    @Test
    fun `given gusts exactly at limit, when scoring, then reason is strong wind`() {
        val result = OutdoorSightseeingScorer.score(idealDay.copy(windGustsMax = t.GUST_LIMIT_KMH))

        assertEquals(ReasonKey.STRONG_WIND, result.reason)
    }

    @Test
    fun `given thunderstorm and rain, when scoring, then storm outranks rain`() {
        val result = OutdoorSightseeingScorer.score(
            idealDay.copy(weatherCode = 95, precipitationSum = t.PRECIP_LIMIT_MM),
        )

        assertEquals(ReasonKey.STORM, result.reason)
    }

    @Test
    fun `given temperature missing, when scoring, then it is reported and reason is insufficient data`() {
        val result = OutdoorSightseeingScorer.score(idealDay.copy(temperatureMax = null))

        assertEquals(ReasonKey.INSUFFICIENT_DATA, result.reason)
        assertEquals(setOf(WeatherFactor.TEMPERATURE), result.missingFactors)
    }

    @Test
    fun `given every measurement missing, when scoring, then score is neutral and all factors reported`() {
        val result = OutdoorSightseeingScorer.score(
            forecast(
                weatherCode = null,
                temperatureMax = null,
                precipitationSum = null,
                windSpeedMax = null,
                windGustsMax = null,
            ),
        )

        assertEquals(ScoreBuilder.NEUTRAL_SCORE, result.score)
        assertEquals(ReasonKey.INSUFFICIENT_DATA, result.reason)
        assertEquals(
            setOf(
                WeatherFactor.WEATHER_CODE,
                WeatherFactor.TEMPERATURE,
                WeatherFactor.PRECIPITATION,
                WeatherFactor.WIND_SPEED,
                WeatherFactor.WIND_GUSTS,
            ),
            result.missingFactors,
        )
    }
}
