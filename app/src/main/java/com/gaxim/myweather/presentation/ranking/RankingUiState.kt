package com.gaxim.myweather.presentation.ranking

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DayRanking
import com.gaxim.myweather.presentation.common.ErrorKind

/** What the ranking screen shows for [city]. */
sealed interface RankingUiState {
    val city: City?

    data class Loading(override val city: City?) : RankingUiState

    data class Success(override val city: City, val days: List<DayRanking>) : RankingUiState

    data class Error(override val city: City?, val kind: ErrorKind) : RankingUiState
}
