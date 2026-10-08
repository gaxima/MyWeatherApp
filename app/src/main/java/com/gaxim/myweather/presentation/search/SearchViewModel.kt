package com.gaxim.myweather.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.usecase.SearchCitiesUseCase
import com.gaxim.myweather.presentation.common.toErrorKind
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCities: SearchCitiesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _events = Channel<SearchEvent>(Channel.BUFFERED)
    val events: Flow<SearchEvent> = _events.receiveAsFlow()

    private var searchJob: Job? = null

    /**
     * Updates the field text right away and searches once the user pauses for [DEBOUNCE_MILLIS].
     * A newer keystroke cancels the pending or in-flight search, so a slow earlier response can
     * never overwrite a newer one.
     */
    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle(query)
            return
        }
        _uiState.update { it.withQuery(query) }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            _uiState.value = SearchUiState.Loading(query)
            _uiState.value = searchCities(query).fold(
                onSuccess = { cities ->
                    if (cities.isEmpty()) SearchUiState.Empty(query)
                    else SearchUiState.Success(query, cities)
                },
                onFailure = { SearchUiState.Error(query, it.toErrorKind()) },
            )
        }
    }

    fun onCitySelected(city: City) {
        viewModelScope.launch { _events.send(SearchEvent.NavigateToRanking(city)) }
    }

    companion object {
        /** How long the user must stop typing before a request is sent. */
        const val DEBOUNCE_MILLIS = 400L
    }
}
