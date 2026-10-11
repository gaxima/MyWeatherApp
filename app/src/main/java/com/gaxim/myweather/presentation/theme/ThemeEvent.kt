package com.gaxim.myweather.presentation.theme

sealed interface ThemeEvent {
    /** The chosen theme could not be stored; the previous one stays in effect. */
    data object SaveFailed : ThemeEvent
}
