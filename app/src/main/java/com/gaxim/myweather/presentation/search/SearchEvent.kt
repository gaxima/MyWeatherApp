package com.gaxim.myweather.presentation.search

import com.gaxim.myweather.domain.model.City

/** One-shot effects of the search screen. */
sealed interface SearchEvent {
    data class NavigateToRanking(val city: City) : SearchEvent
}
