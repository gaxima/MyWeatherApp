package com.gaxim.myweather.domain.repository

import com.gaxim.myweather.domain.model.City

interface CityRepository {
    /** No match is a success with an empty list. */
    suspend fun search(query: String): Result<List<City>>
}
