package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.repository.ForecastRepository

internal class FakeForecastRepository(
    var result: Result<List<DailyForecast>> = Result.success(emptyList()),
) : ForecastRepository {
    val requestedCities = mutableListOf<City>()

    override suspend fun getForecast(city: City): Result<List<DailyForecast>> {
        requestedCities += city
        return result
    }
}
