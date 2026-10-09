package com.gaxim.myweather.domain.repository

import com.gaxim.myweather.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode): Result<Unit>
}
