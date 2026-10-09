package com.gaxim.myweather.domain.repository

import com.gaxim.myweather.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    /** Emits the saved mode, [ThemeMode.System] when nothing valid is saved, and every later change. */
    fun observeThemeMode(): Flow<ThemeMode>

    /** Saves [mode]. A failure to write is reported as a failed [Result], never thrown. */
    suspend fun setThemeMode(mode: ThemeMode): Result<Unit>
}
