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

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val uiState: StateFlow<ThemeUiState> = themeRepository.observeThemeMode()
        .map<ThemeMode, ThemeUiState> { ThemeUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeUiState.Loading)

    fun onToggleTheme() {
        val current = (uiState.value as? ThemeUiState.Loaded)?.mode ?: return
        viewModelScope.launch { themeRepository.setThemeMode(current.next()) }
    }

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch { themeRepository.setThemeMode(mode) }
    }

    private fun ThemeMode.next(): ThemeMode =
        when (this) {
            ThemeMode.System -> ThemeMode.Light
            ThemeMode.Light -> ThemeMode.Dark
            ThemeMode.Dark -> ThemeMode.System
        }
}
