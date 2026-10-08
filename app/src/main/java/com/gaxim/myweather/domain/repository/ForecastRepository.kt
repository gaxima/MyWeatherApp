package com.gaxim.myweather.domain.repository

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DailyForecast

interface ForecastRepository {
    /**
     * The 7-day forecast for [city], one entry per day in date order.
     * Failures are a [com.gaxim.myweather.domain.model.DomainError].
     */
    suspend fun getForecast(city: City): Result<List<DailyForecast>>
}
