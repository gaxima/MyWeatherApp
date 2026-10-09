package com.gaxim.myweather.presentation.theme

import com.gaxim.myweather.domain.model.ThemeMode

sealed interface ThemeUiState {
    /** The saved mode has not been read yet. */
    data object Loading : ThemeUiState

    data class Loaded(val mode: ThemeMode) : ThemeUiState
}
