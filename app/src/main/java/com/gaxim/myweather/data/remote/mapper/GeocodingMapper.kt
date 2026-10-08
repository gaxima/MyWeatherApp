package com.gaxim.myweather.data.remote.mapper

import com.gaxim.myweather.data.remote.dto.CityDto
import com.gaxim.myweather.data.remote.dto.GeocodingResponseDto
import com.gaxim.myweather.domain.model.City

/** Missing `results` means no match, so it maps to an empty list. */
fun GeocodingResponseDto.toDomain(): List<City> = results.orEmpty().mapNotNull { it.toDomain() }

/** Entries without a name or coordinates cannot be forecast, so they are dropped. */
internal fun CityDto.toDomain(): City? {
    val name = name?.takeIf { it.isNotBlank() } ?: return null
    return City(
        name = name,
        country = country,
        region = admin1,
        latitude = latitude ?: return null,
        longitude = longitude ?: return null,
    )
}
