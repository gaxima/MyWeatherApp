package com.gaxim.myweather.presentation.ranking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.usecase.GetActivityRankingUseCase
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.common.toErrorKind
import com.gaxim.myweather.presentation.navigation.RankingRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RankingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getActivityRanking: GetActivityRankingUseCase,
) : ViewModel() {

    private val city: City? = RankingRoute.cityFrom(savedStateHandle)

    private val _uiState = MutableStateFlow<RankingUiState>(RankingUiState.Loading(city))
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private val _events = Channel<RankingEvent>(Channel.BUFFERED)
    val events: Flow<RankingEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    /**
     * Reloads the forecast while keeping the current days on screen. A failure keeps them too and
     * is reported as a [RankingEvent.RefreshFailed]. Ignored unless a forecast is already showing.
     */
    fun refresh() {
        val days = when (val current = _uiState.value) {
            is RankingUiState.Success -> current.days
            is RankingUiState.Refreshing -> current.days
            else -> return
        }
        val city = city ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = RankingUiState.Refreshing(city, days)
            getActivityRanking(city).fold(
                onSuccess = { _uiState.value = RankingUiState.Success(city, it) },
                onFailure = {
                    _uiState.value = RankingUiState.Success(city, days)
                    _events.send(RankingEvent.RefreshFailed(it.toErrorKind()))
                },
            )
        }
    }

    private fun load() {
        val city = city
        if (city == null) {
            // The destination was opened without its arguments; there is nothing to fetch.
            _uiState.value = RankingUiState.Error(null, ErrorKind.UNKNOWN)
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = RankingUiState.Loading(city)
            _uiState.value = getActivityRanking(city).fold(
                onSuccess = { RankingUiState.Success(city, it) },
                onFailure = { RankingUiState.Error(city, it.toErrorKind()) },
            )
        }
    }
}
