package com.gaxim.myweather.domain.repository

import com.gaxim.myweather.domain.model.City

interface CityRepository {
    /**
     * Cities matching [query]. No match is a success with an empty list.
     * Failures are a [com.gaxim.myweather.domain.model.DomainError].
     */
    suspend fun search(query: String): Result<List<City>>
}
