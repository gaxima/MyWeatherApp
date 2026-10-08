package com.gaxim.myweather.data.remote.mapper

import com.gaxim.myweather.data.remote.dto.CityDto
import com.gaxim.myweather.data.remote.dto.GeocodingResponseDto
import com.gaxim.myweather.domain.model.City
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeocodingMapperTest {

    @Test
    fun `given complete result, when mapping, then all fields are carried over`() {
        val dto = GeocodingResponseDto(
            listOf(CityDto("Oslo", "Norway", "Oslo County", 59.91, 10.75)),
        )

        assertEquals(
            listOf(City("Oslo", "Norway", "Oslo County", 59.91, 10.75)),
            dto.toDomain(),
        )
    }

    @Test
    fun `given missing results, when mapping, then result is empty`() {
        assertTrue(GeocodingResponseDto(results = null).toDomain().isEmpty())
    }

    @Test
    fun `given missing optional country and region, when mapping, then they are null`() {
        val city = GeocodingResponseDto(listOf(CityDto(name = "X", latitude = 1.0, longitude = 2.0)))
            .toDomain().single()

        assertEquals(null, city.country)
        assertEquals(null, city.region)
    }

    @Test
    fun `given entries without name or coordinates, when mapping, then they are dropped`() {
        val dto = GeocodingResponseDto(
            listOf(
                CityDto(name = null, latitude = 1.0, longitude = 2.0),
                CityDto(name = "  ", latitude = 1.0, longitude = 2.0),
                CityDto(name = "NoLat", latitude = null, longitude = 2.0),
                CityDto(name = "NoLon", latitude = 1.0, longitude = null),
                CityDto(name = "Valid", latitude = 1.0, longitude = 2.0),
            ),
        )

        assertEquals(listOf("Valid"), dto.toDomain().map { it.name })
    }
}
