package com.gaxim.myweather.domain.repository

import com.gaxim.myweather.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    /** Emits the saved mode, [ThemeMode.System] when nothing valid is saved, and every later change. */
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
