package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.repository.CityRepository

internal fun city(
    name: String = "Oslo",
    country: String? = "Norway",
    region: String? = "Oslo",
    latitude: Double = 59.91,
    longitude: Double = 10.75,
) = City(name, country, region, latitude, longitude)

internal class FakeCityRepository(
    var result: Result<List<City>> = Result.success(emptyList()),
) : CityRepository {
    val queries = mutableListOf<String>()

    override suspend fun search(query: String): Result<List<City>> {
        queries += query
        return result
    }
}
