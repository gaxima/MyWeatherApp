package com.gaxim.myweather.domain.model

/**
 * Why an activity got its score, as a stable key. The presentation layer maps each key to a
 * localized string, which keeps the domain free of Android resources.
 */
enum class ReasonKey {
    SKI_FRESH_SNOW,
    SKI_NO_SNOW,
    SKI_LITTLE_FRESH_SNOW,
    SKI_TOO_WARM,
    SURF_RIDEABLE_WIND,
    SURF_TOO_CALM,
    STRONG_WIND,
    STORM,
    RAIN,
    OUTDOOR_PLEASANT,
    OUTDOOR_TOO_COLD,
    OUTDOOR_TOO_HOT,
    INDOOR_ANY_WEATHER,
    INSUFFICIENT_DATA,
}
