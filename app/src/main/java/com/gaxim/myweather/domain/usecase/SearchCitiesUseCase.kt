package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.repository.CityRepository
import javax.inject.Inject

/**
 * Finds cities matching a user-typed query.
 *
 * A blank query is a successful, empty result and never reaches the repository, so the UI
 * does not spend a request on an empty search box.
 */
class SearchCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(query: String): Result<List<City>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Result.success(emptyList())
        return cityRepository.search(trimmed)
    }
}
