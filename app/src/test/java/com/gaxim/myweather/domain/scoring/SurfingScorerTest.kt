package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfingScorerTest {

    private val t = SurfingThresholds

    private val idealDay = forecast(
        weatherCode = 1,
        temperatureMax = 22.0,
        precipitationSum = 0.0,
        windSpeedMax = (t.WIND_FULL_LOW_KMH + t.WIND_FULL_HIGH_KMH) / 2,
        windGustsMax = t.GUST_IDEAL_KMH,
    )

    @Test
    fun `given rideable wind and nice weather, when scoring surfing, then score is perfect`() {
        val result = SurfingScorer.score(idealDay)

        assertEquals(Activity.SURFING, result.activity)
        assertEquals(100, result.score)
        assertEquals(ReasonKey.SURF_RIDEABLE_WIND, result.reason)
        assertTrue(result.missingFactors.isEmpty())
    }

    @Test
    fun `given thunderstorm with gale and cold, when scoring surfing, then score is zero`() {
        val bad = forecast(
            weatherCode = 95,
            temperatureMax = 0.0,
            precipitationSum = 30.0,
            windSpeedMax = 90.0,
            windGustsMax = 120.0,
        )

        val result = SurfingScorer.score(bad)

        assertEquals(0, result.score)
        assertEquals(ReasonKey.STORM, result.reason)
    }

    @Test
    fun `given dead calm, when scoring surfing, then score drops and reason is too calm`() {
        val result = SurfingScorer.score(idealDay.copy(windSpeedMax = 0.0))

        assertTrue(result.score < 100)
        assertEquals(ReasonKey.SURF_TOO_CALM, result.reason)
    }

    @Test
    fun `given wind exactly at lower edge of ideal band, when scoring, then wind counts fully`() {
        val result = SurfingScorer.score(idealDay.copy(windSpeedMax = t.WIND_FULL_LOW_KMH))

        assertEquals(100, result.score)
        assertEquals(ReasonKey.SURF_RIDEABLE_WIND, result.reason)
    }

    @Test
    fun `given wind just below ideal band, when scoring, then score drops and reason is too calm`() {
        val result = SurfingScorer.score(idealDay.copy(windSpeedMax = t.WIND_FULL_LOW_KMH - 1.0))

        assertTrue(result.score < 100)
        assertEquals(ReasonKey.SURF_TOO_CALM, result.reason)
    }

    @Test
    fun `given wind exactly at upper edge of ideal band, when scoring, then wind counts fully`() {
        val result = SurfingScorer.score(idealDay.copy(windSpeedMax = t.WIND_FULL_HIGH_KMH))

        assertEquals(100, result.score)
    }

    @Test
    fun `given wind just above ideal band, when scoring, then score drops`() {
        val result = SurfingScorer.score(idealDay.copy(windSpeedMax = t.WIND_FULL_HIGH_KMH + 1.0))

        assertTrue(result.score < 100)
    }

    @Test
    fun `given wind exactly at blown-out limit, when scoring, then reason is strong wind`() {
        val result = SurfingScorer.score(idealDay.copy(windSpeedMax = t.WIND_ZERO_HIGH_KMH))

        assertEquals(ReasonKey.STRONG_WIND, result.reason)
    }

    @Test
    fun `given gusts exactly at limit, when scoring, then reason is strong wind`() {
        val result = SurfingScorer.score(idealDay.copy(windGustsMax = t.GUST_LIMIT_KMH))

        assertEquals(ReasonKey.STRONG_WIND, result.reason)
    }

    @Test
    fun `given heavy rain, when scoring, then score is lower than a dry day and reason is rain`() {
        val dry = SurfingScorer.score(idealDay)
        val wet = SurfingScorer.score(
            idealDay.copy(weatherCode = 65, precipitationSum = t.PRECIP_LIMIT_MM),
        )

        assertTrue(wet.score < dry.score)
        assertEquals(ReasonKey.RAIN, wet.reason)
    }

    @Test
    fun `given thunderstorm and strong wind, when scoring, then storm is the reason`() {
        val result = SurfingScorer.score(
            idealDay.copy(weatherCode = 95, windSpeedMax = t.WIND_ZERO_HIGH_KMH),
        )

        assertEquals(ReasonKey.STORM, result.reason)
    }

    @Test
    fun `given wind speed missing, when scoring, then it is reported and reason is insufficient data`() {
        val result = SurfingScorer.score(idealDay.copy(windSpeedMax = null))

        assertEquals(ReasonKey.INSUFFICIENT_DATA, result.reason)
        assertEquals(setOf(WeatherFactor.WIND_SPEED), result.missingFactors)
    }

    @Test
    fun `given every measurement missing, when scoring, then score is neutral and all factors reported`() {
        val result = SurfingScorer.score(
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
