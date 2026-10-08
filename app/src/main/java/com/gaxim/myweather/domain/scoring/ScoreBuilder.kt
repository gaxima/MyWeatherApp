package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.WeatherFactor
import kotlin.math.roundToInt

/**
 * Combines weighted factors into a 0..100 score.
 *
 * Each factor contributes `weight * fraction`, where fraction is 0..1. A factor whose input is
 * missing (null fraction) is skipped: the remaining weights are renormalized and the factor is
 * recorded in [missingFactors] so the UI can disclose it.
 */
internal class ScoreBuilder {
    private var earned = 0.0
    private var available = 0.0
    private val missing = mutableSetOf<WeatherFactor>()

    val missingFactors: Set<WeatherFactor> get() = missing

    fun factor(factor: WeatherFactor, weight: Double, fraction: Double?) {
        if (fraction == null) {
            missing += factor
            return
        }
        earned += weight * fraction.coerceIn(0.0, 1.0)
        available += weight
    }

    fun score(): Int =
        if (available == 0.0) NEUTRAL_SCORE else (earned / available * MAX_SCORE).roundToInt()

    companion object {
        const val MAX_SCORE = 100

        /** Score when no factor is available at all: neither good nor bad. */
        const val NEUTRAL_SCORE = 50
    }
}

/**
 * Linear 0..1 fraction of [value] between [worst] (0.0) and [best] (1.0), clamped.
 * Works in either direction, e.g. `ramp(wind, worst = 60.0, best = 20.0)`. Null stays null.
 */
internal fun ramp(value: Double?, worst: Double, best: Double): Double? =
    value?.let { ((it - worst) / (best - worst)).coerceIn(0.0, 1.0) }
