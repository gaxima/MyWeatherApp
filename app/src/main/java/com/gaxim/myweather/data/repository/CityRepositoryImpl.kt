package com.gaxim.myweather.data.repository

import com.gaxim.myweather.data.remote.GeocodingApi
import com.gaxim.myweather.data.remote.mapper.toDomain
import com.gaxim.myweather.data.util.safeCall
import com.gaxim.myweather.di.IoDispatcher
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

class CityRepositoryImpl @Inject constructor(
    private val api: GeocodingApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : CityRepository {

    override suspend fun search(query: String): Result<List<City>> =
        safeCall(dispatcher) { api.search(query).toDomain() }
}
