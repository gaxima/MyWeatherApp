package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.WeatherFactor
import kotlin.math.roundToInt

/**
 * Sums `weight * fraction` per factor into a 0..100 score. A null fraction is skipped: the
 * remaining weights are renormalized and the factor is recorded in [missingFactors].
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

/** Linear 0..1 position of [value] between [worst] and [best], in either direction. */
internal fun ramp(value: Double?, worst: Double, best: Double): Double? =
    value?.let { rampValue(it, worst, best) }

/** Trapezoid for factors where both too little and too much are bad. */
internal fun band(
    value: Double?,
    zeroLow: Double,
    fullLow: Double,
    fullHigh: Double,
    zeroHigh: Double,
): Double? = value?.let { minOf(rampValue(it, zeroLow, fullLow), rampValue(it, zeroHigh, fullHigh)) }

private fun rampValue(value: Double, worst: Double, best: Double): Double =
    ((value - worst) / (best - worst)).coerceIn(0.0, 1.0)
