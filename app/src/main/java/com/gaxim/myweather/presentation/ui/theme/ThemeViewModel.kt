package com.gaxim.myweather.presentation.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.usecase.ObserveThemeModeUseCase
import com.gaxim.myweather.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds the saved theme for the whole app.
 *
 * [themeMode] is `null` only until the first read of the store completes, so the UI can wait
 * instead of drawing the default theme first and flashing to the saved one.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode?> = observeThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    /** Moves to the next mode in the System -> Light -> Dark cycle and saves it. */
    fun onToggleTheme() {
        val current = themeMode.value ?: return
        viewModelScope.launch { setThemeMode(current.next()) }
    }
}
