package com.gaxim.myweather.presentation.theme

import com.gaxim.myweather.domain.model.ThemeMode

sealed interface ThemeUiState {
    data object Loading : ThemeUiState

    data class Loaded(val mode: ThemeMode) : ThemeUiState
}
