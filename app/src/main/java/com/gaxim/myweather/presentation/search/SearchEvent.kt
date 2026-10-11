package com.gaxim.myweather.presentation.search

import com.gaxim.myweather.domain.model.City

sealed interface SearchEvent {
    data class NavigateToRanking(val city: City) : SearchEvent
}
