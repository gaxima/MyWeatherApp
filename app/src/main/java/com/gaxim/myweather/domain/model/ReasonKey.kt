package com.gaxim.myweather.domain.model

/**
 * Why an activity got its score, as a stable key. The presentation layer maps each key to a
 * localized string, which keeps the domain free of Android resources.
 */
enum class ReasonKey {
    SKI_FRESH_SNOW,
    SKI_LITTLE_FRESH_SNOW,
    SKI_TOO_WARM,
    SURF_RIDEABLE_WIND,
    SURF_TOO_CALM,
    STRONG_WIND,
    STORM,
    RAIN,
    INSUFFICIENT_DATA,
}
