package com.gaxim.myweather.presentation.search

import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.presentation.common.ErrorKind

/** What the search screen shows. Every state carries the text currently typed in the field. */
sealed interface SearchUiState {
    val query: String

    /** Nothing typed yet. */
    data class Idle(override val query: String = "") : SearchUiState

    data class Loading(override val query: String) : SearchUiState

    data class Success(override val query: String, val cities: List<City>) : SearchUiState

    /** The search worked but matched no city. */
    data class Empty(override val query: String) : SearchUiState

    data class Error(override val query: String, val kind: ErrorKind) : SearchUiState
}

internal fun SearchUiState.withQuery(query: String): SearchUiState = when (this) {
    is SearchUiState.Idle -> copy(query = query)
    is SearchUiState.Loading -> copy(query = query)
    is SearchUiState.Success -> copy(query = query)
    is SearchUiState.Empty -> copy(query = query)
    is SearchUiState.Error -> copy(query = query)
}
