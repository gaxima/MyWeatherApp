package com.gaxim.myweather.di

import com.gaxim.myweather.data.repository.CityRepositoryImpl
import com.gaxim.myweather.data.repository.ForecastRepositoryImpl
import com.gaxim.myweather.domain.repository.CityRepository
import com.gaxim.myweather.domain.repository.ForecastRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindCityRepository(impl: CityRepositoryImpl): CityRepository

    @Binds
    fun bindForecastRepository(impl: ForecastRepositoryImpl): ForecastRepository
}
