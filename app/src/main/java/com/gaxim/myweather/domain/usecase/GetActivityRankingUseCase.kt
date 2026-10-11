package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DayRanking
import com.gaxim.myweather.domain.repository.ForecastRepository
import com.gaxim.myweather.domain.scoring.ActivityRanker
import javax.inject.Inject

/** Ranks the activities for each forecast day; repository failures pass through untouched. */
class GetActivityRankingUseCase @Inject constructor(
    private val forecastRepository: ForecastRepository,
    private val ranker: ActivityRanker,
) {
    suspend operator fun invoke(city: City): Result<List<DayRanking>> =
        forecastRepository.getForecast(city).map { days -> days.map(ranker::rank) }
}
