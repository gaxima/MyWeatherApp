package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.repository.CityRepository
import javax.inject.Inject

/** A blank query returns an empty success without reaching the repository. */
class SearchCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(query: String): Result<List<City>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Result.success(emptyList())
        return cityRepository.search(trimmed)
    }
}
