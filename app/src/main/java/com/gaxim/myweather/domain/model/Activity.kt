package com.gaxim.myweather.domain.model

/**
 * Rankable activities. Declaration order is significant: it is the deterministic
 * tie-break when two activities have the same score (earlier wins).
 */
enum class Activity {
    SKIING,
    SURFING,
    OUTDOOR_SIGHTSEEING,
    INDOOR_SIGHTSEEING,
}
