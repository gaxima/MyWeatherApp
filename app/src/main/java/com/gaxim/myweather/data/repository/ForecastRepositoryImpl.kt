package com.gaxim.myweather.data.repository

import com.gaxim.myweather.data.remote.ForecastApi
import com.gaxim.myweather.data.remote.mapper.toDomain
import com.gaxim.myweather.di.IoDispatcher
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.repository.ForecastRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

class ForecastRepositoryImpl @Inject constructor(
    private val api: ForecastApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ForecastRepository {

    override suspend fun getForecast(city: City): Result<List<DailyForecast>> =
        safeCall(dispatcher) { api.getForecast(city.latitude, city.longitude).toDomain() }
}
