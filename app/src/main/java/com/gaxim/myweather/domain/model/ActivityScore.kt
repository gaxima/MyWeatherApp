package com.gaxim.myweather.domain.model

/**
 * @property score 0 (poor) to 100 (ideal).
 * @property missingFactors inputs that were unavailable and left out of the score, which the UI
 * should disclose.
 */
data class ActivityScore(
    val activity: Activity,
    val score: Int,
    val reason: ReasonKey,
    val missingFactors: Set<WeatherFactor>,
)
