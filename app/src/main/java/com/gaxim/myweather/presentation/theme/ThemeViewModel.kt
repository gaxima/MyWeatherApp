package com.gaxim.myweather.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds the saved theme for the whole app.
 *
 * The state stays [ThemeUiState.Loading] until the first read of the store completes, so the UI
 * can wait instead of drawing the default theme first and flashing to the saved one.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val uiState: StateFlow<ThemeUiState> = themeRepository.observeThemeMode()
        .map<ThemeMode, ThemeUiState> { ThemeUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeUiState.Loading)

    /**
     * Moves to the next mode in the toggle cycle and saves it.
     *
     * If saving fails the state is simply not updated, so the button keeps showing the mode that
     * is actually stored.
     */
    fun onToggleTheme() {
        val current = (uiState.value as? ThemeUiState.Loaded)?.mode ?: return
        viewModelScope.launch { themeRepository.setThemeMode(current.next()) }
    }

    /** Toggle cycle: System -> Light -> Dark -> System. */
    private fun ThemeMode.next(): ThemeMode = ThemeMode.entries[(ordinal + 1) % ThemeMode.entries.size]
}
