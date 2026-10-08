package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.ReasonKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ActivityRankerTest {

    private val ranker = ActivityRanker()

    private fun ranked(forecast: DailyForecast) = ranker.rank(forecast).scores.map { it.activity }

    @Test
    fun `given any forecast, when ranking, then all four activities appear exactly once`() {
        val result = ranked(forecast())

        assertEquals(Activity.entries.toSet(), result.toSet())
        assertEquals(Activity.entries.size, result.size)
    }

    @Test
    fun `given any forecast, when ranking, then scores are sorted descending`() {
        val scores = ranker.rank(forecast(weatherCode = 61, precipitationSum = 6.0)).scores.map { it.score }

        assertEquals(scores.sortedDescending(), scores)
    }

    @Test
    fun `given rain, when ranking, then indoor beats outdoor`() {
        val result = ranked(forecast(weatherCode = 63, precipitationSum = 12.0))

        assertTrue(result.indexOf(Activity.INDOOR_SIGHTSEEING) < result.indexOf(Activity.OUTDOOR_SIGHTSEEING))
    }

    @Test
    fun `given clear mild day, when ranking, then outdoor beats indoor`() {
        val result = ranked(forecast(weatherCode = 0, temperatureMax = 22.0))

        assertTrue(result.indexOf(Activity.OUTDOOR_SIGHTSEEING) < result.indexOf(Activity.INDOOR_SIGHTSEEING))
    }

    @Test
    fun `given cold snowy day, when ranking, then skiing comes first`() {
        val result = ranked(
            forecast(weatherCode = 73, temperatureMax = -4.0, snowfallSum = 8.0, precipitationSum = 6.0),
        )

        assertEquals(Activity.SKIING, result.first())
    }

    @Test
    fun `given breezy warm sunny day, when ranking, then surfing beats skiing`() {
        val result = ranked(forecast(weatherCode = 1, temperatureMax = 24.0, windSpeedMax = 20.0))

        assertTrue(result.indexOf(Activity.SURFING) < result.indexOf(Activity.SKIING))
    }

    @Test
    fun `given equal scores, when ranking, then activity declaration order breaks the tie`() {
        val scorersInReverse = Activity.entries.reversed().map { FixedScorer(it, score = 50) }

        val result = ActivityRanker(scorersInReverse).rank(forecast()).scores.map { it.activity }

        assertEquals(Activity.entries.toList(), result)
    }

    @Test
    fun `given partial tie, when ranking, then higher score wins and tie keeps declaration order`() {
        val scorers = listOf(
            FixedScorer(Activity.INDOOR_SIGHTSEEING, 70),
            FixedScorer(Activity.SURFING, 70),
            FixedScorer(Activity.SKIING, 10),
            FixedScorer(Activity.OUTDOOR_SIGHTSEEING, 90),
        )

        val result = ActivityRanker(scorers).rank(forecast()).scores.map { it.activity }

        assertEquals(
            listOf(
                Activity.OUTDOOR_SIGHTSEEING,
                Activity.SURFING,
                Activity.INDOOR_SIGHTSEEING,
                Activity.SKIING,
            ),
            result,
        )
    }

    @Test
    fun `given every measurement missing, when ranking, then all tie and order is declaration order`() {
        val allMissing = forecast(
            weatherCode = null,
            temperatureMax = null,
            precipitationSum = null,
            snowfallSum = null,
            windSpeedMax = null,
            windGustsMax = null,
        )

        assertEquals(Activity.entries.toList(), ranked(allMissing))
    }

    @Test
    fun `given the same forecast, when ranking twice, then results are identical`() {
        val input = forecast(weatherCode = 61, precipitationSum = 4.0, windSpeedMax = 35.0)

        assertEquals(ranker.rank(input), ranker.rank(input))
    }

    @Test
    fun `given a forecast date, when ranking, then the ranking carries that date`() {
        val date = LocalDate.of(2026, 7, 4)

        assertEquals(date, ranker.rank(forecast(date = date)).date)
    }

    private class FixedScorer(private val activity: Activity, private val score: Int) : ActivityScorer {
        override fun score(forecast: DailyForecast) =
            ActivityScore(activity, score, ReasonKey.INSUFFICIENT_DATA, emptySet())
    }
}
