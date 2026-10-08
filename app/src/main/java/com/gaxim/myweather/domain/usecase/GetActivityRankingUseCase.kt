package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DayRanking
import com.gaxim.myweather.domain.repository.ForecastRepository
import com.gaxim.myweather.domain.scoring.ActivityRanker
import javax.inject.Inject

/**
 * Fetches the forecast for a city and ranks the activities for each day.
 *
 * Days keep the order the repository returns them in. A repository failure is passed through
 * untouched so callers see the original [com.gaxim.myweather.domain.model.DomainError].
 */
class GetActivityRankingUseCase @Inject constructor(
    private val forecastRepository: ForecastRepository,
    private val ranker: ActivityRanker,
) {
    suspend operator fun invoke(city: City): Result<List<DayRanking>> =
        forecastRepository.getForecast(city).map { days -> days.map(ranker::rank) }
}
