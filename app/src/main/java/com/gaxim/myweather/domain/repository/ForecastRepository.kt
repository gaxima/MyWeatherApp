package com.gaxim.myweather.domain.repository

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DailyForecast

interface ForecastRepository {
    /** One entry per day, in date order. */
    suspend fun getForecast(city: City): Result<List<DailyForecast>>
}
