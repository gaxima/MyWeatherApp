package com.gaxim.myweather.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val uiState: StateFlow<ThemeUiState> = themeRepository.observeThemeMode()
        .map<ThemeMode, ThemeUiState> { ThemeUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeUiState.Loading)

    private val _events = Channel<ThemeEvent>(Channel.BUFFERED)
    val events: Flow<ThemeEvent> = _events.receiveAsFlow()

    /** On failure the stored mode stays in [uiState] and [ThemeEvent.SaveFailed] is emitted. */
    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode).onFailure { _events.send(ThemeEvent.SaveFailed) }
        }
    }
}
