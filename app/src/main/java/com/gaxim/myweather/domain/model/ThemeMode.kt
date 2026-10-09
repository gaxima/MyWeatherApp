package com.gaxim.myweather.domain.model

/** The color theme the user asked for. [System] defers to the device setting. */
enum class ThemeMode {
    System,
    Light,
    Dark;

    /** The mode the toggle moves to next: System -> Light -> Dark -> System. */
    fun next(): ThemeMode = entries[(ordinal + 1) % entries.size]
}
