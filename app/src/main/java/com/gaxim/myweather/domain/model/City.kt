package com.gaxim.myweather.domain.model

/** A searchable place, as resolved by geocoding. */
data class City(
    val name: String,
    val country: String?,
    val region: String?,
    val latitude: Double,
    val longitude: Double,
)
