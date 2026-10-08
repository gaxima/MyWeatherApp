package com.gaxim.myweather.domain.model

/**
 * Suitability of one [activity] for one day.
 *
 * @property score 0 (poor) to 100 (ideal).
 * @property reason the dominant reason for the score.
 * @property missingFactors forecast inputs this activity uses that were unavailable. They were
 * left out of the score, so the UI should tell the user the result ignores them.
 */
data class ActivityScore(
    val activity: Activity,
    val score: Int,
    val reason: ReasonKey,
    val missingFactors: Set<WeatherFactor>,
)
