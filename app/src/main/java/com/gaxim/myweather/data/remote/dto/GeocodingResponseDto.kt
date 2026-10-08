package com.gaxim.myweather.data.remote.dto

import kotlinx.serialization.Serializable

/** Open-Meteo omits `results` entirely when nothing matches, hence the null default. */
@Serializable
data class GeocodingResponseDto(
    val results: List<CityDto>? = null,
)

@Serializable
data class CityDto(
    val name: String? = null,
    val country: String? = null,
    val admin1: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
