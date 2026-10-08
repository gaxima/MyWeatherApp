package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.DayRanking

/**
 * Scores every activity for a day and orders them best first.
 *
 * Ties are broken by activity declaration order, so the result is deterministic regardless of
 * the order of [scorers].
 */
class ActivityRanker(
    private val scorers: List<ActivityScorer> = DEFAULT_SCORERS,
) {
    fun rank(forecast: DailyForecast): DayRanking {
        val scores = scorers
            .map { it.score(forecast) }
            .sortedWith(
                compareByDescending<ActivityScore> { it.score }.thenBy { it.activity.ordinal },
            )
        return DayRanking(date = forecast.date, scores = scores)
    }

    private companion object {
        val DEFAULT_SCORERS = listOf(
            SkiingScorer,
            SurfingScorer,
            OutdoorSightseeingScorer,
            IndoorSightseeingScorer,
        )
    }
}
