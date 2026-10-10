package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkiingScorerTest {

    private val idealDay = forecast(
        temperatureMax = -3.0,
        snowfallSum = SkiingThresholds.SNOWFALL_IDEAL_CM,
        windSpeedMax = 10.0,
        windGustsMax = 30.0,
    )

    @Test
    fun `given fresh snow, cold and calm, when scoring skiing, then score is perfect`() {
        val result = SkiingScorer.score(idealDay)

        assertEquals(Activity.SKIING, result.activity)
        assertEquals(100, result.score)
        assertEquals(ReasonKey.SKI_FRESH_SNOW, result.reason)
        assertTrue(result.missingFactors.isEmpty())
    }

    @Test
    fun `given warm dry windy day, when scoring skiing, then score is zero`() {
        val bad = forecast(
            temperatureMax = 12.0,
            snowfallSum = 0.0,
            windSpeedMax = SkiingThresholds.WIND_LIMIT_KMH + 10,
            windGustsMax = SkiingThresholds.GUST_LIMIT_KMH + 10,
        )

        assertEquals(0, SkiingScorer.score(bad).score)
    }

    @Test
    fun `given no snowfall but calm cold day, when scoring skiing, then score is zero and reason is no snow`() {
        val result = SkiingScorer.score(idealDay.copy(snowfallSum = 0.0))

        assertEquals(0, result.score)
        assertEquals(ReasonKey.SKI_NO_SNOW, result.reason)
    }

    @Test
    fun `given tropical day with no snowfall, when scoring skiing, then score is zero`() {
        val result = SkiingScorer.score(
            forecast(temperatureMax = 30.0, snowfallSum = 0.0, windSpeedMax = 10.0, windGustsMax = 20.0),
        )

        assertEquals(0, result.score)
        assertEquals(ReasonKey.SKI_NO_SNOW, result.reason)
    }

    @Test
    fun `given snowfall exactly at ideal, when scoring, then snow counts fully`() {
        val result = SkiingScorer.score(idealDay)

        assertEquals(ReasonKey.SKI_FRESH_SNOW, result.reason)
    }

    @Test
    fun `given snowfall just below ideal, when scoring, then score drops and reason is little snow`() {
        val result = SkiingScorer.score(
            idealDay.copy(snowfallSum = SkiingThresholds.SNOWFALL_IDEAL_CM - 0.1),
        )

        assertTrue(result.score < 100)
        assertEquals(ReasonKey.SKI_LITTLE_FRESH_SNOW, result.reason)
    }

    @Test
    fun `given temperature exactly at warm limit, when scoring, then reason is too warm`() {
        val result = SkiingScorer.score(
            idealDay.copy(temperatureMax = SkiingThresholds.TEMP_WARM_LIMIT_C),
        )

        assertEquals(ReasonKey.SKI_TOO_WARM, result.reason)
    }

    @Test
    fun `given temperature just below warm limit, when scoring, then reason is not too warm`() {
        val result = SkiingScorer.score(
            idealDay.copy(temperatureMax = SkiingThresholds.TEMP_WARM_LIMIT_C - 0.1),
        )

        assertEquals(ReasonKey.SKI_FRESH_SNOW, result.reason)
    }

    @Test
    fun `given wind exactly at limit, when scoring, then reason is strong wind`() {
        val result = SkiingScorer.score(
            idealDay.copy(windSpeedMax = SkiingThresholds.WIND_LIMIT_KMH),
        )

        assertEquals(ReasonKey.STRONG_WIND, result.reason)
    }

    @Test
    fun `given gusts exactly at limit, when scoring, then reason is strong wind`() {
        val result = SkiingScorer.score(
            idealDay.copy(windGustsMax = SkiingThresholds.GUST_LIMIT_KMH),
        )

        assertEquals(ReasonKey.STRONG_WIND, result.reason)
    }

    @Test
    fun `given one missing measurement, when scoring, then it is skipped and reported`() {
        val result = SkiingScorer.score(idealDay.copy(windGustsMax = null))

        assertEquals(100, result.score)
        assertEquals(setOf(WeatherFactor.WIND_GUSTS), result.missingFactors)
    }

    @Test
    fun `given snowfall missing, when scoring, then reason is insufficient data`() {
        val result = SkiingScorer.score(idealDay.copy(snowfallSum = null))

        assertEquals(ReasonKey.INSUFFICIENT_DATA, result.reason)
        assertEquals(setOf(WeatherFactor.SNOWFALL), result.missingFactors)
    }

    @Test
    fun `given every measurement missing, when scoring, then score is neutral and all factors reported`() {
        val result = SkiingScorer.score(
            forecast(
                temperatureMax = null,
                snowfallSum = null,
                windSpeedMax = null,
                windGustsMax = null,
            ),
        )

        assertEquals(ScoreBuilder.NEUTRAL_SCORE, result.score)
        assertEquals(ReasonKey.INSUFFICIENT_DATA, result.reason)
        assertEquals(
            setOf(
                WeatherFactor.SNOWFALL,
                WeatherFactor.TEMPERATURE,
                WeatherFactor.WIND_SPEED,
                WeatherFactor.WIND_GUSTS,
            ),
            result.missingFactors,
        )
    }
}
