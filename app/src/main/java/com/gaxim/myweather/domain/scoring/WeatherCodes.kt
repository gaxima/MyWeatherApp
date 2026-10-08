package com.gaxim.myweather.domain.scoring

/**
 * Interprets WMO weather interpretation codes as returned by Open-Meteo `weather_code`.
 *
 * Friendliness is how pleasant the sky is for being outdoors, 0.0 (hostile) to 1.0 (clear).
 * The values are judgement calls, kept here as named constants so they are easy to tune.
 */
internal object WeatherCodes {
    private const val CLEAR = 1.0
    private const val PARTLY_CLOUDY = 0.8
    private const val OVERCAST = 0.6
    private const val FOG = 0.4
    private const val DRIZZLE = 0.3
    private const val RAIN_OR_SNOW = 0.1
    private const val THUNDERSTORM = 0.0

    /** Used for codes outside the documented WMO set: neither good nor bad. */
    const val UNKNOWN_FRIENDLINESS = 0.5

    /** Null when [code] is missing, so the caller can report the factor as unavailable. */
    fun outdoorFriendliness(code: Int?): Double? = code?.let {
        when (it) {
            0, 1 -> CLEAR
            2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            in 51..57 -> DRIZZLE
            in 61..67, in 71..77, in 80..86 -> RAIN_OR_SNOW
            in 95..99 -> THUNDERSTORM
            else -> UNKNOWN_FRIENDLINESS
        }
    }

    fun isThunderstorm(code: Int?): Boolean = code != null && code in 95..99
}
